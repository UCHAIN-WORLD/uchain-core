package com.uchain.network;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uchain.main.Settings;
import com.uchain.network.NetworkUtil.Disconnected;
import com.uchain.network.NetworkUtil.GetHandlerToPeerConnectionManager;
import com.uchain.network.NetworkUtil.PeerHandler;
import com.uchain.network.peer.PeerConnectionManager;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.io.Inet;
import akka.io.Tcp;
import akka.io.Tcp.Bound;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connect;
import akka.io.Tcp.Connected;
import akka.io.TcpMessage;
import akka.io.TcpSO;

public class NetworkManager extends AbstractActor {

	private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
	private Settings settings;

	private ActorRef tcpManager;

	private ActorRef peerHandlerActor;

	private ActorRef nodeActor;
	
	private String[] localAddress;

	private Set<InetSocketAddress> outgoing = new HashSet<InetSocketAddress>();

	public NetworkManager(Settings settings, ActorRef peerHandlerActor,ActorRef nodeActor) {
		this.peerHandlerActor = peerHandlerActor;
		this.settings = settings;
		this.nodeActor = nodeActor;
		localAddress = settings.getBindAddress().split(":");
	}

	public static Props props(Settings settings, ActorRef peerHandlerActor,ActorRef nodeActor) {
		return Props.create(NetworkManager.class, settings, peerHandlerActor,nodeActor);
	}

	@Override
	public void preStart() throws Exception {
		if (tcpManager == null) {
			tcpManager = Tcp.get(getContext().system()).manager();
		}
		InetSocketAddress inetSocketAddress = new InetSocketAddress(localAddress[0], Integer.parseInt(localAddress[1]));
		tcpManager = Tcp.get(getContext().getSystem()).manager();
		final List<Inet.SocketOption> options = new ArrayList<Inet.SocketOption>();
		options.add(TcpSO.keepAlive(true));
		tcpManager.tell(TcpMessage.bind(getSelf(), inetSocketAddress, 100, options, false), getSelf());
	}

	ActorRef peerConnectionManagerActor = null;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Bound.class, msg -> {
			log.info("成功绑定到端口 " + localAddress[1]);
			if (settings.getKnownPeers() != null && !"".equals(settings.getKnownPeers())) {
				String[] knownPeers = settings.getKnownPeers().split(",");
				for (String knownPeer : knownPeers) {
					log.info("Connecting to:" + knownPeer);
					InetSocketAddress inetSocketAddress = new InetSocketAddress(knownPeer.split(":")[0],
							Integer.parseInt(knownPeer.split(":")[1]));
					outgoing.add(inetSocketAddress);
					final List<Inet.SocketOption> options = new ArrayList<Inet.SocketOption>();
					options.add(TcpSO.keepAlive(true));
					Duration connTimeout = Duration.ofSeconds(Long.parseLong(settings.getConnectionTimeout()));
					tcpManager.tell(TcpMessage.connect(inetSocketAddress, null, options, connTimeout, true), getSelf());
				}
			}
		}).match(CommandFailed.class, msg -> {
			if ("Bind".equals(msg.cmd().getClass().getSimpleName())) {
				log.error("端口 " + localAddress[1] + " already in use!");
				getContext().stop(getSelf());
			} else {
				Connect connect = (Connect) msg.cmd();
				outgoing.remove(connect.remoteAddress());
				log.info("未能连接到 : " + connect.remoteAddress());
				Disconnected disconnected = new Disconnected(connect.remoteAddress());
				peerHandlerActor.tell(disconnected, getSelf());
			}
		}).match(Connected.class, msg -> {
			InetSocketAddress direction = null;
			String logMsg = "";
			if (outgoing.contains(msg.remoteAddress())) {
				direction = msg.remoteAddress();
				logMsg = "输入的远程连接 " + msg.remoteAddress() + " 绑定到本地" + msg.localAddress();
			} else {
				logMsg = "传入的远程连接 " + msg.remoteAddress() + " 绑定到本地 " + msg.localAddress();
			}
			log.info(logMsg);
			ActorRef connection = getSender();

			ActorSystem peerConnectionManagerSystem = ActorSystem.create("peerConnectionManagerSystem");
			peerConnectionManagerActor = peerConnectionManagerSystem.actorOf(PeerConnectionManager.props(settings,
					peerHandlerActor, nodeActor,connection, direction, msg.remoteAddress(), getSelf()), "peerConnectionManager");

			outgoing.remove(msg.remoteAddress());
		}).match(GetHandlerToPeerConnectionManager.class, msg -> {
			PeerHandler peerHandler = new PeerHandler(peerConnectionManagerActor);
			peerHandlerActor.tell(peerHandler, getSelf());// handler做为消息发送到peerHandlerManager，由peerHandlerManager统一管理
		}).build();
	}
}
