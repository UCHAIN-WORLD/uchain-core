package com.uchain.network.peer;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Tcp;
import com.uchain.main.Settings;
import com.uchain.network.NetworkUtil.*;
import com.uchain.network.message.BlockMessageImpl.BlockMessage;
import com.uchain.network.message.BlockMessageImpl.InventoryMessage;
import com.uchain.network.message.BlockMessageImpl.VersionMessage;
import com.uchain.network.message.MessagePack;
import com.uchain.util.NetworkTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PeerHandlerManager extends AbstractActor {
    private static Logger log = LoggerFactory.getLogger(PeerHandlerManager.class);
    private Settings settings;
    private NetworkTimeProvider timeProvider;
    private ActorRef tcpManager;
    private int lastIdUsed = 0;

    //握手成功
    private Map<InetSocketAddress, ConnectedPeer> connectedPeers = new HashMap<>();

    //握手前
    private Set<InetSocketAddress> connectingPeers = new HashSet<>();


    public PeerHandlerManager(Settings settings, NetworkTimeProvider timeProvider) {
        this.settings = settings;
        this.timeProvider = timeProvider;
    }

    public static Props props(Settings settings, NetworkTimeProvider timeProvider) {
        return Props.create(PeerHandlerManager.class, settings, timeProvider);
    }


    @Override
    public void preStart() {
        if (tcpManager == null) {
            tcpManager = Tcp.get(getContext().system()).manager();
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //TODO: peerList and apiInterface
                //connection
                .match(DoConnecting.class, msg -> {
                    log.info(msg.getRemote() + ":" + msg.getDirection());
                    ActorRef peerConnectionHandler = getSender();
                    boolean isIncoming = false;
                    if (msg.getDirection() instanceof Incoming) {
                        isIncoming = true;
                    }
                    boolean isAlreadyConnecting = connectingPeers.contains(msg.getRemote());
                    if (isAlreadyConnecting && !isIncoming) {
                        log.info("尝试连接两次 " + msg.getRemote() + ", 将删除重复连接");
                        peerConnectionHandler.tell(new CloseConnection(), getSelf());
                    } else {
                        if (!isIncoming) {
                            log.info("远程链接 " + msg.getRemote());
                            connectingPeers.add(msg.getRemote());
                        }
                        peerConnectionHandler.tell(new StartInteraction(), getSelf());
                        lastIdUsed += 1;
                    }
                })
                .match(PeerHandler.class, msg -> {
                    /*ActorRef handler = msg.getHandlerRef();
                    Message message = new Message("1", "message_test");
                    //获取远程hangler，测发送消息
                    handler.tell(message, getSelf());*/
                })
                .match(MessagePack.class, msg -> {
                    if (msg.getAddress() != null) {
                        ConnectedPeer peer = connectedPeers.get(msg.getAddress());
                        if (peer != null) {
                            peer.getHandlerRef().tell(new MessagePack(msg.getMessageType(), msg.getData(), null), getSelf());
                        } else {
                            log.error("peer(" + msg.getAddress() + ") not exists");
                        }
                    } else {
                        connectedPeers.forEach((socketAddress, connectedPeer) -> {
                            connectedPeer.getHandlerRef().tell(new MessagePack(msg.getMessageType(), msg.getData(), null), getSelf());
                        });
                    }
                })
                //handshaked
                .match(Handshaked.class, msg -> {
                    ConnectedPeer peer = msg.getConnectedPeer();
                    if (peer.getDirection() instanceof Outgoing && settings.getBindAddress().equals(peer.getSocketAddress().getAddress() + ":" + peer.getSocketAddress().getPort())) {
                        peer.getHandlerRef().tell(new CloseConnection(), getSelf());
                    }else {
                        connectedPeers.put(peer.getSocketAddress(), peer);
                        // log.info("更新本节点连接的节点="+connectedPeers);
                        Thread.sleep(50);
                        peer.getHandlerRef().tell(new VersionMessage(0).pack(), getSelf());
                    }
                })
                .match(BlockMessage.class, msg -> {
                    log.info("broadcasting BlockMessage:");
                    //发送到PeerConnectionManager的workingCycle，由这里发送到其他节点
                    connectedPeers.forEach((socketAddress, connectedPeer) -> {
                        connectedPeer.getHandlerRef().tell(msg.pack(), getSelf());
                        log.info("send block " + msg.getBlock().height() + "(" + msg.getBlock().id() + ")" /*to "+connectedPeer.toString()*/);
                    });
                })
                .match(InventoryMessage.class, msg ->
                        connectedPeers.forEach((socketAddress, connectedPeer) -> connectedPeer.getHandlerRef().tell(msg.pack(), getSelf())))
                //disconnection
                .match(Disconnected.class, msg -> {
                    connectedPeers.remove(msg.getRemoteAddress());
                    connectingPeers.remove(msg.getRemoteAddress());
                })
                .build();
    }
}
