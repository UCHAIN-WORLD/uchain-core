package com.uchain.networkmanager;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Inet;
import akka.io.Tcp;
import akka.io.Tcp.Bound;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connect;
import akka.io.Tcp.Connected;
import akka.io.TcpMessage;
import akka.io.TcpSO;
import com.uchain.core.ChainInfo;
import com.uchain.main.Settings;
import com.uchain.networkmanager.NetworkUtil.Disconnected;
import com.uchain.networkmanager.NetworkUtil.GetHandlerToPeerConnectionManager;
import com.uchain.networkmanager.NetworkUtil.PeerHandler;
import com.uchain.networkmanager.peermanager.PeerConnectionManager;
import com.uchain.util.NetworkTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkManager extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
    private Settings settings;
    private ChainInfo chainInfo;
    private NetworkTimeProvider timeProvider;
    private ActorRef tcpManager;
    private ActorRef node = getContext().getParent();
    private ActorRef peerHandlerActor;

    private String[] localAddress;

    private Set<InetSocketAddress> outgoing = new HashSet<>();

    public NetworkManager(Settings settings, ChainInfo chainInfo, NetworkTimeProvider timeProvider, ActorRef peerHandlerActor) {
        this.settings = settings;
        this.chainInfo = chainInfo;
        this.timeProvider = timeProvider;
        this.peerHandlerActor = peerHandlerActor;
        localAddress = settings.getBindAddress().split(":");
    }

    public static Props props(Settings settings, ChainInfo chainInfo, NetworkTimeProvider timeProvider, ActorRef peerHandlerActor) {
        return Props.create(NetworkManager.class, settings, chainInfo, timeProvider, peerHandlerActor);
    }

    @Override
    public void preStart() {
        if (tcpManager == null) {
            tcpManager = Tcp.get(getContext().system()).manager();
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(localAddress[0], Integer.parseInt(localAddress[1]));
        tcpManager = Tcp.get(getContext().getSystem()).manager();
        final List<Inet.SocketOption> options = new ArrayList<Inet.SocketOption>();
        options.add(TcpSO.keepAlive(true));
        tcpManager.tell(TcpMessage.bind(getSelf(), inetSocketAddress, 100, options, false), getSelf());
    }

    ActorRef peerConnectionManagerActor = context().system().actorOf(Props.empty());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //bind
                .match(Bound.class, msg -> {
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
                    /*if ("Bind".equals(msg.cmd().getClass().getSimpleName())) {
                        log.error("端口 " + localAddress[1] + " already in use!");
                        getContext().stop(getSelf());
                    } else {
                        Connect connect = (Connect) msg.cmd();
                        outgoing.remove(connect.remoteAddress());
                        log.info("未能连接到 : " + connect.remoteAddress());
                        Disconnected disconnected = new Disconnected(connect.remoteAddress());
                        peerHandlerActor.tell(disconnected, getSelf());
                    }*/

                    if(msg.cmd() instanceof Tcp.Bind){
                        log.error("端口 " + localAddress[1] + " already in use!");
                        getContext().stop(getSelf());
                    }else if(msg.cmd() instanceof Tcp.Connect){
                        Connect connect = (Connect) msg.cmd();
                        outgoing.remove(connect.remoteAddress());
                        log.info("未能连接到 : " + connect.remoteAddress());
                        Disconnected disconnected = new Disconnected(connect.remoteAddress());
                        peerHandlerActor.tell(disconnected, getSelf());
                    }else if(msg.cmd() instanceof Tcp.Command){
                        log.info("执行命令失败 : " + msg.cmd());
                    }else{
                        log.warn("NetworkController: 未知的错误 - "+ msg.cmd());
                    }
                })
                .match(Connected.class, msg -> {
                    NetworkUtil.ConnectionType direction = null;
                    String logMsg = "";
                    if (outgoing.contains(msg.remoteAddress())) {
                        direction = new NetworkUtil.Outgoing();
                        logMsg = "输入的远程连接 " + msg.remoteAddress() + " 绑定到本地" + msg.localAddress();
                    } else {
                        direction = new NetworkUtil.Incoming();
                        logMsg = "传入的远程连接 " + msg.remoteAddress() + " 绑定到本地 " + msg.localAddress();
                    }
                    log.info(logMsg);
                    ActorRef connection = getSender();

//			ActorSystem peerConnectionManagerSystem = ActorSystem.create("peerConnectionManagerSystem");
                    peerConnectionManagerActor = context().actorOf(PeerConnectionManager.props(settings,
                            peerHandlerActor, node, chainInfo, connection, direction, msg.remoteAddress(), timeProvider));
                    outgoing.remove(msg.remoteAddress());
                })
                .match(NetworkUtil.DisconnectFrom.class, msg->{
                    log.info("Disconnected from "+msg.getPeer().getSocketAddress());
                    NetworkUtil.ConnectedPeer peer = msg.getPeer();
                    peer.getHandlerRef().tell(new NetworkUtil.CloseConnection(), getSelf());
                    peerHandlerActor.tell(new Disconnected(peer.getSocketAddress()),getSelf());
                })
                //PeerConnection
                .match(GetHandlerToPeerConnectionManager.class, msg -> {
                    PeerHandler peerHandler = new PeerHandler(peerConnectionManagerActor);
                    peerHandlerActor.tell(peerHandler, getSelf());// handler做为消息发送到peerHandlerManager，由peerHandlerManager统一管理
                }).build();
    }
}
