package com.uchain.main;

import java.io.IOException;

import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.LevelDBBlockChainBuilder;
import com.uchain.core.producer.Producer;
import com.uchain.network.NetworkManager;
import com.uchain.network.Node;
import com.uchain.network.peer.PeerHandlerManager;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class MainApp {
	public static void main(String[] args) throws IOException {
		Settings settings = new Settings("config2");
//		UPnP upnp = new UPnP(settings);

		ActorSystem uchainSystem = ActorSystem.create("uchainSystem");
		ActorRef peerHandlerActor = uchainSystem.actorOf(PeerHandlerManager.props(settings), "peerHandlerManager");
		
		LevelDBBlockChain chain = LevelDBBlockChainBuilder.populate(settings);
		ActorRef nodeActor = uchainSystem.actorOf(Node.props(chain, peerHandlerActor), "nodeManager");

		uchainSystem.actorOf(Producer.props(settings.getConsensusSettings(),chain,peerHandlerActor));

		uchainSystem.actorOf(NetworkManager.props(settings,peerHandlerActor,nodeActor), "networkManager");
	}
}
