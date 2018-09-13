package com.uchain.network.message;

import java.math.BigInteger;
import java.net.InetSocketAddress;

import com.uchain.core.Block;
import com.uchain.core.producer.ProduceState;
import com.uchain.network.message.BlockMessageImpl.BlockMessage;
import com.uchain.network.message.BlockMessageImpl.GetBlocksMessage;
import com.uchain.network.message.BlockMessageImpl.GetDataMessage;
import com.uchain.network.message.BlockMessageImpl.InventoryMessage;
import com.uchain.network.message.BlockMessageImpl.VersionMessage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessagePack {
	private MessageType messageType;
	private byte[] data;
	private InetSocketAddress address;

	public MessagePack(MessageType messageType, byte[] data, InetSocketAddress address) {
		this.messageType = messageType;
		this.data = data;
		this.address = address;
	}

	public static PackMessage fromBytes(byte[] bytes, InetSocketAddress addr) {
		MessageType messageTypeValue = MessageType.getMessageTypeByValue(bytes[0]);
		byte[] data = new byte[bytes.length - 1];
		System.arraycopy(bytes, 1, data, 0, data.length);
		PackMessage packMessage = null;
		switch (messageTypeValue) {
			case Version: 
				packMessage = new VersionMessage(new BigInteger(data).intValue());
				break;
			case GetBlocks:
				packMessage = new GetBlocksMessage(GetBlocksPayload.fromBytes(data));
				break;
			case Block:
				packMessage = new BlockMessage(Block.fromBytes(data));
				break;
			case Inventory:
				packMessage = new InventoryMessage(Inventory.fromBytes(data));
				break;
			case Getdata:
				packMessage = new GetDataMessage(Inventory.fromBytes(data));
				break;
		default:
			break;
		}
		return packMessage;
	}

}
