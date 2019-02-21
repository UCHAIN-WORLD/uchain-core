package com.uchain.networkmanager.peermanager;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.io.Tcp;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.ConnectionClosed;
import akka.io.Tcp.Received;
import akka.io.TcpMessage;
import akka.util.ByteString;
import akka.util.ByteStringBuilder;
import akka.util.CompactByteString;
import com.uchain.core.ChainInfo;
import com.uchain.main.Settings;
import com.uchain.networkmanager.NetworkUtil.*;
import com.uchain.networkmanager.message.MessagePack;
import com.uchain.networkmanager.message.MessageType;
import com.uchain.util.NetworkTimeProvider;
import com.uchain.util.Object2Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.nio.ByteOrder;

public class PeerConnectionManager extends AbstractActor {
    Logger log = LoggerFactory.getLogger(PeerConnectionManager.class);
    private Settings settings;
    private ActorRef peerHandlerActor;
    private ActorRef connection;
    private ConnectionType direction;
    private InetSocketAddress remote;
    private ActorRef networkManager;
    private Cancellable handshakeTimeoutCancellableOpt;
    private boolean handshakeSent = false;
    private boolean handshakeGot = false;
    private Handshake receivedHandshake;
    private ChainInfo chainInfo;
    private ActorRef nodeRef;
    private NetworkTimeProvider timeProvider;
    private ByteString chunksBuffer;

    public PeerConnectionManager(Settings settings, ActorRef peerHandlerActor, ActorRef nodeRef,
                                 ChainInfo chainInfo, ActorRef connection,
                                 ConnectionType direction, InetSocketAddress remote, NetworkTimeProvider timeProvider) {
        this.settings = settings;
        this.peerHandlerActor = peerHandlerActor;
        this.connection = connection;
        this.direction = direction;
        this.remote = remote;
        this.chainInfo = chainInfo;
        this.nodeRef = nodeRef;
        this.timeProvider = timeProvider;
        this.chunksBuffer = CompactByteString.empty();
        networkManager = getContext().parent();
//        getContext().watch(peerHandlerActor);
        getContext().watch(connection);
//		getContext().watch(networkManager);
    }

    public static Props props(Settings settings, ActorRef peerHandlerActor, ActorRef nodeRef,
                              ChainInfo chainInfo, ActorRef connection,
                              ConnectionType direction, InetSocketAddress remote, NetworkTimeProvider timeProvider) {
        return Props.create(PeerConnectionManager.class, settings, peerHandlerActor, nodeRef, chainInfo, connection, direction, remote,
                timeProvider);
    }

    @Override
    public void preStart() {
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
        return receiveBuilder()
                .match(DoConnecting.class, msg -> {
                    log.info(msg.getRemote() + ":" + msg.getDirection());
                })
                //processErrors
                .match(CommandFailed.class, msg -> {
                    if (msg.cmd() instanceof Tcp.Command) {
                        log.info("执行命令失败 : " + msg.cmd() + " remote : " + remote);
                        connection.tell(TcpMessage.resumeReading(), getSelf());
                    } else if (msg.cmd() instanceof Tcp.Write) {
                        log.warn("写入失败 :$w " + remote);
                        connection.tell(TcpMessage.close(), getSelf());
                        connection.tell(TcpMessage.resumeReading(), getSelf());
                        connection.tell(TcpMessage.resumeWriting(), getSelf());
                    }
                })
                .match(ConnectionClosed.class, msg -> {
                    Disconnected disconnected = new Disconnected(remote);
                    peerHandlerActor.tell(disconnected, getSelf());
                    log.info("链接关闭 : " + remote + ": " + msg.getErrorCause());
                    getContext().stop(getSelf());
                })
                .match(CloseConnection.class, msg -> {
                    log.info("强制中止通信: " + remote);
                    connection.tell(TcpMessage.close(), getSelf());
                })
                .match(StartInteraction.class, msg -> {
                    Handshake handshake = new Handshake(settings.getAgentName(), settings.getAppVersion(),
                            settings.getNodeName(),chainInfo.getId(),System.currentTimeMillis());
                    connection.tell(TcpMessage.register(connection), getSelf());
                    connection.tell(TcpMessage.write(ByteString.fromArray(Object2Array.objectToByteArray(handshake))),
                            getSelf());
                    log.info("发送握手到:" + remote);
                    handshakeSent = true;
                    if (handshakeGot && handshakeSent)
                        getSelf().tell(new HandshakeDone(), getSelf());
                })
                .match(Received.class, msg -> {
                    try {
                        receivedHandshake = (Handshake) Object2Array.byteArrayToObject((((Received) msg).data()).toArray());
                        handleHandshake(receivedHandshake);
                    } catch (Exception e) {
                        log.info("解析握手时的错误");
                        e.printStackTrace();
                        getSelf().tell(new CloseConnection(), getSelf());
                    }
                })
                .match(HandshakeTimeout.class, msg -> {
                    log.info("与远程" + remote + "握手超时, 将删除连接");
                    getSelf().tell(new CloseConnection(), getSelf());
                })
                .match(HandshakeDone.class, msg -> {
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

    private void sendMessagePack(MessagePack msg, final ActorRef connection) {
        ByteStringBuilder builder = ByteString.createBuilder();

        builder.putInt(msg.getData().length + 1, ByteOrder.BIG_ENDIAN);
        builder.putByte((byte) MessageType.getMessageTypeByType(msg.getMessageType()));
        builder.putBytes(msg.getData());

        connection.tell(TcpMessage.write(builder.result()), getSelf());
    }

    private void processChunksBuffer() {
        if (chunksBuffer.length() > 5) {
            int payloadLen = chunksBuffer.iterator().getInt(ByteOrder.BIG_ENDIAN);
            log.debug("payloadLen=" + payloadLen + " chunksBuffer.length=" + chunksBuffer.length());
            if (chunksBuffer.length() >= payloadLen + 4) {
                chunksBuffer = chunksBuffer.drop(4);
                nodeRef.tell(MessagePack.fromBytes(chunksBuffer, null), ActorRef.noSender());
                chunksBuffer = chunksBuffer.drop(payloadLen);
                processChunksBuffer();
            } else
                log.info("not enough data, payloadLen=" + payloadLen + " chunksBuffer.length=" + chunksBuffer.length());
        }
    }

    private Receive workingCycle(final ActorRef connection) {
        return receiveBuilder().match(MessagePack.class, msg -> {
//			log.info("发送的消息类型:" + msg.getMessageType());
            connection.tell(TcpMessage.register(connection), getSelf());   // what for ???
            sendMessagePack(msg, connection);
        }).match(Received.class, msg -> {
//			log.info("接收的消息:" + msg);
            chunksBuffer = chunksBuffer.concat(msg.data());
            processChunksBuffer();
            connection.tell(TcpMessage.resumeReading(), getSelf());
        }).build();
    }

    @Override
    public void postStop() {
        Disconnected disconnected = new Disconnected(remote);
        peerHandlerActor.tell(disconnected, getSelf());
        log.info("Peer handler to " + remote + "销毁");
    }

    public boolean  handleHandshake(Handshake handshakeMsg){
        if (!chainInfo.getId().equals(handshakeMsg.getChainId())) {
            log.error("Peer on a different chain. Closing connection");
            context().self().tell(new CloseConnection(),getSelf());
            return false;
        } else {
            long myTime = System.currentTimeMillis();
            long timeGap = Math.abs(handshakeMsg.getTime() - myTime);
            log.info("peer timeGap = "+timeGap);
            if (timeGap > Long.parseLong(settings.getPeerMaxTimeGap())) {
                log.error("peer timeGap too large "+timeGap+"  Closing connection");
                context().self().tell(new CloseConnection(),getSelf());
                return false;
            }else{
                log.info("获得握手:" + remote);
                connection.tell(TcpMessage.resumeReading(), getSelf());
                networkManager.tell(new GetHandlerToPeerConnectionManager(), getSelf()); // 握手成功后，向PeerConnectionManager发送远程handler
                if (receivedHandshake != null)
                    handshakeGot = true;
                if (handshakeGot && handshakeSent)
                    getSelf().tell(new HandshakeDone(), getSelf());
                return true;
            }
        }
    }
}
