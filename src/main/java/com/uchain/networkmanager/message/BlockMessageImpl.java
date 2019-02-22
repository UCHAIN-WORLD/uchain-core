package com.uchain.networkmanager.message;

import com.uchain.common.Serializabler;
import com.uchain.core.block.Block;
import com.uchain.core.producerblock.SendRawTransaction;
import com.uchain.cryptohash.UInt160;
import com.uchain.cryptohash.UInt256;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

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
    public static class BlocksMessage implements PackMessage {
        private MessageType messageType;
        private BlocksPayload blocksPayload;

        public BlocksMessage(BlocksPayload blocksPayload) {
            this.messageType = MessageType.Blocks;
            this.blocksPayload = blocksPayload;
        }

        @Override
        public MessagePack pack() {
            return new MessagePack(messageType, Serializabler.toBytes(blocksPayload), null);
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
		private InventoryPayload inv;

		public GetDataMessage(InventoryPayload inv) {
			this.messageType = MessageType.Getdata;
			this.inv = inv;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, Serializabler.toBytes(inv), null);
		}
	}

    @Getter
    @Setter
    public static class TransactionsMessage implements PackMessage {
        private MessageType messageType;
        private TransactionsPayload txs;

        public TransactionsMessage(TransactionsPayload txs) {
            this.messageType = MessageType.Transactions;
            this.txs = txs;
        }

        @Override
        public MessagePack pack() {
            return new MessagePack(messageType, Serializabler.toBytes(txs), null);
        }
    }

    public interface RPCCommandMessage extends PackMessage{}
	@Getter
	@Setter
	public static class GetBlocksCmd implements RPCCommandMessage{
		private MessageType messageType;

		public GetBlocksCmd() {
			this.messageType = MessageType.RPCCommand;
		}

		@Override
		public MessagePack pack() {
			return null;
		}
	}

	@Getter
	@Setter
	public static class GetBlockByIdCmd implements RPCCommandMessage {
		private MessageType messageType;
		private UInt256 id;

		public GetBlockByIdCmd(){
			this.messageType = MessageType.RPCCommand;
		}

		public GetBlockByIdCmd(UInt256 id){
			this.messageType = MessageType.RPCCommand;
			this.id = id;
		}
		@Override
		public MessagePack pack() {
			return null;
		}
	}

	@Getter
	@Setter
	public static class GetBlockByHeightCmd implements RPCCommandMessage {
		private MessageType messageType;
		private Integer height;

		public GetBlockByHeightCmd(){
			this.messageType = MessageType.RPCCommand;
		}

		public GetBlockByHeightCmd(Integer height){
			this.messageType = MessageType.RPCCommand;
			this.height = height;
		}
		@Override
		public MessagePack pack() {
			return null;
		}
	}
	@Getter
	@Setter
	public static class GetBlockCountCmd implements RPCCommandMessage {
		private MessageType messageType;

		public GetBlockCountCmd(){
			this.messageType = MessageType.RPCCommand;
		}
		@Override
		public MessagePack pack() {
			return null;
		}
	}

	@Getter
	@Setter
	public static class GetAccountCmd implements RPCCommandMessage {
		private MessageType messageType;
		private UInt160 address;

		public GetAccountCmd(){
			this.messageType = MessageType.RPCCommand;
		}

		public GetAccountCmd(UInt160 address){
			this.messageType = MessageType.RPCCommand;
			this.address = address;
		}
		@Override
		public MessagePack pack() {
			return null;
		}
	}

	@Getter
	@Setter
	public static class SendRawTransactionCmd implements RPCCommandMessage {
		private MessageType messageType;
		private SendRawTransaction rawTx;
		public SendRawTransactionCmd(){
			this.messageType = MessageType.RPCCommand;
		}

		public SendRawTransactionCmd(SendRawTransaction tx){
			this.messageType = MessageType.RPCCommand;
			this.rawTx = tx;
		}
		@Override
		public MessagePack pack() {
			return null;
		}
	}

	@Getter
	@Setter
	public static class GetTransactionCmd implements RPCCommandMessage {
		private MessageType messageType;
		private UInt256 id;
		public GetTransactionCmd(){
			this.messageType = MessageType.RPCCommand;
		}

		public GetTransactionCmd(UInt256 tx){
			this.messageType = MessageType.RPCCommand;
			this.id = tx;
		}
		@Override
		public MessagePack pack() {
			return null;
		}
	}

	@Getter
	@Setter
	public static class GetTransactionReceiptCmd implements RPCCommandMessage {
		private MessageType messageType;
		private UInt256 id;
		public GetTransactionReceiptCmd(UInt256 tx){
			this.messageType = MessageType.RPCCommand;
			this.id = tx;
		}

		public GetTransactionReceiptCmd(){
			this.messageType = MessageType.RPCCommand;
		}

		@Override
		public MessagePack pack() {
			return null;
		}
	}
}
