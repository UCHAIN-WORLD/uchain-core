package com.uchain.main;

import java.io.IOException;

import com.uchain.network.NetworkManager;
import com.uchain.network.peer.PeerHandlerManager;
import com.uchain.network.upnp.UPnP;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class MainApp {
	public static void main(String[] args) throws IOException {
		Settings settings = new Settings("config2");
//		UPnP upnp = new UPnP(settings);
		
		ActorSystem peerHandlerManagerSystem = ActorSystem.create("peerHandlerManagerSystem");
		ActorRef peerHandlerActor = peerHandlerManagerSystem.actorOf(PeerHandlerManager.props(settings), "peerHandlerManager");
		
		ActorSystem networkManagerSystem = ActorSystem.create("networkManagerSystem");
		ActorRef printerActor = networkManagerSystem.actorOf(NetworkManager.props(settings,peerHandlerActor), "networkManager");
		
//		ActorSystem clientActorSystem = ActorSystem.create("ClientActorSystem");
//
//        ActorRef clientActor = clientActorSystem.actorOf(ClientActor.props(
//                new InetSocketAddress("localhost", 9084), null), "clientActor");
	}
}
