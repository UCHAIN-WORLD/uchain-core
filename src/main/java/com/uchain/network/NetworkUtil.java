package com.uchain.network;

import java.io.Serializable;
import java.net.InetSocketAddress;

import akka.actor.ActorRef;
import lombok.Getter;
import lombok.Setter;

public class NetworkUtil {

	@Getter
	@Setter
	public static class DoConnecting {
		private InetSocketAddress direction;

		private InetSocketAddress remote;

	}

	public static class CloseConnection {
	}

	public static class HandshakeTimeout {
	}

	public static class StartInteraction {
	}

	public static class HandshakeDone {
	}

	@Getter
	@Setter
	public static class ConnectedPeer {
		private InetSocketAddress socketAddress;
		private ActorRef handlerRef;
		private InetSocketAddress direction;
		private Handshake handshake;

		public ConnectedPeer(InetSocketAddress socketAddress, ActorRef handlerRef, InetSocketAddress direction,
				Handshake handshake) {
			this.socketAddress = socketAddress;
			this.handlerRef = handlerRef;
			this.direction = direction;
			this.handshake = handshake;
		}

		@Override
		public String toString() {
			return "ConnectedPeer{" +
					"socketAddress=" + socketAddress +
					", handlerRef=" + handlerRef +
					", direction=" + direction +
					", handshake=" + handshake +
					'}';
		}
	}

	@Getter
	@Setter
	public static class Handshaked {
		private ConnectedPeer connectedPeer;

		public Handshaked(ConnectedPeer connectedPeer) {
			this.connectedPeer = connectedPeer;
		}
	}

	@Getter
	@Setter
	public static class Handshake implements Serializable {

		private static final long serialVersionUID = 1L;
		private String agentName;
		private String appVersion;
		private String nodeName;

		public Handshake(String agentName, String appVersion, String nodeName) {
			this.agentName = agentName;
			this.appVersion = appVersion;
			this.nodeName = nodeName;
		}

		@Override
		public String toString() {
			return "Handshake [agentName=" + agentName + ", appVersion=" + appVersion + ", nodeName=" + nodeName + "]";
		}
	}

	@Getter
	@Setter
	public static class Message implements Serializable {

		@Override
		public String toString() {
			return "Message [type=" + type + ", context=" + context + "]";
		}

		private static final long serialVersionUID = 2L;
		private String type;
		private String context;

		public Message(String type, String context) {
			this.type = type;
			this.context = context;
		}
	}

	public static class GetHandlerToPeerConnectionManager {
	}

	@Getter
	@Setter
	public static class PeerHandler {
		private ActorRef handlerRef;

		public PeerHandler(ActorRef handlerRef) {
			this.handlerRef = handlerRef;
		}
	}

	@Getter
	@Setter
	public static class Disconnected {
		private InetSocketAddress remoteAddress;

		public Disconnected(InetSocketAddress remoteAddress) {
			this.remoteAddress = remoteAddress;
		}
	}
}
