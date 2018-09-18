package com.uchain.main;

import java.io.IOException;

import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.LevelDBBlockChainBuilder;
import com.uchain.network.NetworkManager;
import com.uchain.network.Node;
import com.uchain.network.peer.PeerHandlerManager;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class MainApp {
	public static void main(String[] args) throws IOException {
		Settings settings = new Settings("config");
//		UPnP upnp = new UPnP(settings);

		ActorSystem peerHandlerManagerSystem = ActorSystem.create("peerHandlerManagerSystem");
		ActorRef peerHandlerActor = peerHandlerManagerSystem.actorOf(PeerHandlerManager.props(settings), "peerHandlerManager");
		
		LevelDBBlockChain chain = LevelDBBlockChainBuilder.populate(settings);
		ActorSystem nodeSystem = ActorSystem.create("nodeSystem");
		ActorRef nodeActor = nodeSystem.actorOf(Node.props(chain, peerHandlerActor), "nodeManager");
				
		ActorSystem networkManagerSystem = ActorSystem.create("networkManagerSystem");
		ActorRef printerActor = networkManagerSystem.actorOf(NetworkManager.props(settings,peerHandlerActor,nodeActor), "networkManager");
		
//		ActorSystem clientActorSystem = ActorSystem.create("ClientActorSystem");
//
//        ActorRef clientActor = clientActorSystem.actorOf(ClientActor.props(
//                new InetSocketAddress("localhost", 9084), null), "clientActor");
	}
}
