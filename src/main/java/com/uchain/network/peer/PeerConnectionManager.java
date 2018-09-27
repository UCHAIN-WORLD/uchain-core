package com.uchain.network.peer;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.ConnectionClosed;
import akka.io.Tcp.Received;
import akka.io.TcpMessage;
import akka.util.ByteString;
import com.uchain.main.Settings;
import com.uchain.network.NetworkUtil.*;
import com.uchain.network.message.MessagePack;
import com.uchain.network.message.MessageType;
import com.uchain.util.Object2Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PeerConnectionManager extends AbstractActor {
	Logger log = LoggerFactory.getLogger(PeerConnectionManager.class);
	private Settings settings;
	private ActorRef peerHandlerActor;
	private ActorRef connection;
	private InetSocketAddress direction;
	private InetSocketAddress remote;
	private ActorRef networkManager;
	private ActorRef nodeActor;
	private Cancellable handshakeTimeoutCancellableOpt;
	private boolean handshakeSent = false;
	private boolean handshakeGot = false;
	private Handshake receivedHandshake;

	public PeerConnectionManager(Settings settings, ActorRef peerHandlerActor, ActorRef nodeActor, ActorRef connection,
			InetSocketAddress direction, InetSocketAddress remote, ActorRef networkManager) {
		this.settings = settings;
		this.peerHandlerActor = peerHandlerActor;
		this.connection = connection;
		this.direction = direction;
		this.remote = remote;
		this.networkManager = networkManager;
		this.nodeActor = nodeActor;
		getContext().watch(peerHandlerActor);
		getContext().watch(connection);
		getContext().watch(networkManager);
	}

	public static Props props(Settings settings, ActorRef peerHandlerActor, ActorRef nodeActor,ActorRef connection,
			InetSocketAddress direction, InetSocketAddress remote, ActorRef networkManager) {
		return Props.create(PeerConnectionManager.class, settings, peerHandlerActor, nodeActor,connection, direction, remote,
				networkManager);
	}

	@Override
	public void preStart() throws Exception {
		DoConnecting doConnecting = new DoConnecting();
		doConnecting.setDirection(direction);
		doConnecting.setRemote(remote);
		peerHandlerActor.tell(doConnecting, getSelf());
		
		handshakeTimeoutCancellableOpt = getContext().system().scheduler().scheduleOnce(
				Duration.create(Long.parseLong(settings.getHandshakeTimeout()), TimeUnit.SECONDS), new Runnable() {
					public void run() {
						getSelf().tell(new HandshakeTimeout(), getSelf());
					}
				}, getContext().system().dispatcher());
		connection.tell(TcpMessage.register(getSelf(), false, true), getSelf());
		connection.tell(TcpMessage.resumeReading(), getSelf());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(DoConnecting.class, msg -> {
			log.info(msg.getRemote() + ":" + msg.getDirection());
		}).match(CommandFailed.class, msg -> {
			log.warn("写入失败 :$w " + remote);
			connection.tell(TcpMessage.close(), getSelf());
			connection.tell(TcpMessage.resumeReading(), getSelf());
			connection.tell(TcpMessage.resumeWriting(), getSelf());
		}).match(ConnectionClosed.class, msg -> {
			Disconnected disconnected = new Disconnected(remote);
			peerHandlerActor.tell(disconnected, getSelf());
			log.info("链接关闭 : " + remote + ": " + msg.getErrorCause());
			getContext().stop(getSelf());
		}).match(Received.class, msg -> {
			try {
				receivedHandshake = (Handshake) Object2Array.byteArrayToObject((((Received) msg).data()).toArray());
				log.info("获得握手:" + remote);
				connection.tell(TcpMessage.resumeReading(), getSelf());
				networkManager.tell(new GetHandlerToPeerConnectionManager(), getSelf()); // 握手成功后，向PeerConnectionManager发送远程handler
				if (receivedHandshake != null)
					handshakeGot = true;
				if (handshakeGot && handshakeSent)
					getSelf().tell(new HandshakeDone(), getSelf());
			} catch (Exception e) {
				log.info("解析握手时的错误");
				e.printStackTrace();
				getSelf().tell(new CloseConnection(), getSelf());
			}
		}).match(CloseConnection.class, msg -> {
			log.info("强制中止通信: " + remote);
			connection.tell(TcpMessage.close(), getSelf());
		}).match(HandshakeTimeout.class, msg -> {
			log.info("与远程" + remote + "握手超时, 将删除连接");
			getSelf().tell(new CloseConnection(), getSelf());
		}).match(StartInteraction.class, msg -> {
			Handshake handshake = new Handshake(settings.getAgentName(), settings.getAppVersion(),
					settings.getNodeName());
			connection.tell(TcpMessage.register(connection), getSelf());
			connection.tell(TcpMessage.write(ByteString.fromArray(Object2Array.objectToByteArray(handshake))),
					getSelf());
			log.info("发送握手到:" + remote);
			handshakeSent = true;
			if (handshakeGot && handshakeSent)
				getSelf().tell(new HandshakeDone(), getSelf());
		}).match(HandshakeDone.class, msg -> {
			if (receivedHandshake == null)
				return;
			ConnectedPeer peer = new ConnectedPeer(remote, getSelf(), direction, receivedHandshake);
			Handshaked handshaked = new Handshaked(peer);
			peerHandlerActor.tell(handshaked, getSelf());
			handshakeTimeoutCancellableOpt.cancel();
			connection.tell(TcpMessage.resumeReading(), getSelf());
			getContext().become(workingCycle(connection));
		}).build();
	}

	private Receive workingCycle(final ActorRef connection) {
		return receiveBuilder().match(MessagePack.class, msg -> {
			log.info("发送的消息类型:" + msg.getMessageType());
			byte[] messageTypeid = new byte[]{(byte)MessageType.getMessageTypeByType(msg.getMessageType())};
			byte[] bt = new byte[messageTypeid.length + msg.getData().length];
	        System.arraycopy(messageTypeid, 0, bt, 0, messageTypeid.length);
	        System.arraycopy(msg.getData(), 0, bt, messageTypeid.length, msg.getData().length);
			System.out.println(Arrays.toString(bt));
			connection.tell(TcpMessage.register(connection), getSelf());
			connection.tell(TcpMessage.write(ByteString.fromArray(bt)), getSelf());
			log.info("发送的消息:" + ByteString.fromArray(bt));
		}).match(Received.class, msg -> {
			log.info("接收的消息:" + msg);
			ByteString data = msg.data();
			connection.tell(TcpMessage.resumeReading(), getSelf());
			nodeActor.tell(MessagePack.fromBytes(data, null), ActorRef.noSender());
		}).build();
	}
	
	@Override
	public void postStop() throws Exception {
		log.info("Peer handler to "+remote +"销毁");
	}
}
