package com.uchain.network.message;

import java.math.BigInteger;
import java.util.Arrays;

import com.uchain.common.Serializabler;
import com.uchain.core.Block;

import lombok.Getter;
import lombok.Setter;

public class BlockMessageImpl {
	@Getter
	@Setter
	public static class VersionMessage implements PackMessage {
		private MessageType messageType;
		private int height;

		public VersionMessage(int height) {
			this.messageType = MessageType.Version;
			this.height = height;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, new BigInteger(height + "").toByteArray(), null);
		}
	}

	@Getter
	@Setter
	public static class GetBlocksMessage implements PackMessage {
		private MessageType messageType;
		private GetBlocksPayload blockHashs;

		public GetBlocksMessage(GetBlocksPayload blockHashs) {
			this.messageType = MessageType.GetBlocks;
			this.blockHashs = blockHashs;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, Serializabler.toBytes(blockHashs), null);
		}

	}

	@Getter
	@Setter
	public static class BlockMessage implements PackMessage {
		private MessageType messageType;
		private Block block;

		public BlockMessage(Block block) {
			this.messageType = MessageType.Block;
			this.block = block;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, Serializabler.toBytes(block), null);
		}
	}

	@Getter
	@Setter
	public static class InventoryMessage implements PackMessage {
		private MessageType messageType;
		private InventoryPayload inv;

		public InventoryMessage(InventoryPayload inv) {
			this.messageType = MessageType.Inventory;
			this.inv = inv;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, Serializabler.toBytes(inv), null);
		}
	}

	@Getter
	@Setter
	public static class GetDataMessage implements PackMessage {
		private MessageType messageType;
		private Inventory inv;

		public GetDataMessage(Inventory inv) {
			this.messageType = MessageType.Getdata;
			this.inv = inv;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, Serializabler.toBytes(inv), null);
		}
	}
}
