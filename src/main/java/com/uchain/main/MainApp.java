package com.uchain.main;

import java.io.IOException;

import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.LevelDBBlockChainBuilder;
import com.uchain.core.producer.Producer;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.CryptoUtil;
import com.uchain.crypto.UInt160;
import com.uchain.network.NetworkManager;
import com.uchain.network.Node;
import com.uchain.network.peer.PeerHandlerManager;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class MainApp {
	public static void main(String[] args) throws IOException {
		Settings settings = new Settings(args[0]);
//		UInt160 to = UInt160.fromBytes(Crypto.hash160(CryptoUtil.listTobyte(new BinaryData(settings.getChainSettings().getChain_miner()).getData())));
//		UInt160 to1 = UInt160.fromBytes(Crypto.hash160(CryptoUtil.listTobyte(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322").getData())));
//		System.out.println(settings.getChainSettings().getChain_miner());
//		System.out.println(new BinaryData(settings.getChainSettings().getChain_miner()).getData().size());
//		System.out.println(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322").getData().size());
//		UPnP upnp = new UPnP(settings);

		ActorSystem uchainSystem = ActorSystem.create("uchainSystem");
		ActorRef peerHandlerActor = uchainSystem.actorOf(PeerHandlerManager.props(settings), "peerHandlerManager");

		LevelDBBlockChain chain = LevelDBBlockChainBuilder.populate(settings);
		ActorRef nodeActor = uchainSystem.actorOf(Node.props(chain, peerHandlerActor), "nodeManager");

		uchainSystem.actorOf(Producer.props(settings.getConsensusSettings(),chain,peerHandlerActor));

		uchainSystem.actorOf(NetworkManager.props(settings,peerHandlerActor,nodeActor), "networkManager");
	}
}
