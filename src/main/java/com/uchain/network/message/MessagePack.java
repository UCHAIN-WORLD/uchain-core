package com.uchain.network.message;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Arrays;

import akka.util.ByteString;
import com.uchain.core.Block;
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

	public static PackMessage fromBytes(ByteString msg, InetSocketAddress addr) {
		MessageType messageTypeValue = MessageType.getMessageTypeByValue(msg.head());
		ByteString data = msg.drop(1);
		PackMessage packMessage = null;
		switch (messageTypeValue) {
			case Version: 
				packMessage = new VersionMessage(new BigInteger(data.toArray()).intValue());
				break;
			case GetBlocks:
				packMessage = new GetBlocksMessage(GetBlocksPayload.fromBytes(data.toArray()));
				break;
			case Block:
				packMessage = new BlockMessage(Block.fromBytes(data.toArray()));
				break;
			case Inventory:
				packMessage = new InventoryMessage(InventoryPayload.fromBytes(data.toArray()));
				break;
			case Getdata:
				packMessage = new GetDataMessage(Inventory.fromBytes(data.toArray()));
				break;
		default:
			break;
		}
		return packMessage;
	}

	public static void main(String[] args) {
		byte[] hh = {0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 102, 15, -77, 112, -80, 88, 50, -32, 39, 20, -102, -43, -33, -20, -107, 115, 43, -114, 45, -54, 80, 24, 102, -73, 108, -83, 9, -114, -126, -53, 122, -2, 36, -41, -73, -65, -111, -27, 17, 69, -92, 82, -46, -126, 107, -63, 58, -122, -37, -120, -33, 113, 125, 0, -11, 61, -31, 19, 79, -1, -90, 50, -62, -28, -90, -89, 30, -33, 53, 0, 0, 0, 33, 2, 42, -64, 26, 30, -87, 39, 82, 65, 97, 94, -90, 54, -100, -123, -76, 30, 32, 22, -85, -60, 116, -123, -20, 97, 108, 60, 88, 63, 27, -110, -91, -56, 0, 0, 0, 71, 48, 69, 2, 33, 0, -48, 102, -119, -81, 127, -60, -37, -61, -24, -99, -78, -90, -108, -31, -26, 60, 15, 126, 52, 70, -50, 56, 26, -46, 23, 3, -114, 33, -64, -42, 101, -36, 2, 32, 25, 67, -107, 35, -13, 56, 123, 102, -17, -117, 69, 88, 83, -120, 109, -6, 71, -50, 6, 58, 79, -115, 26, -32, 89, -111, -89, -79, -127, -86, -40, 92, 5, -85, 125, 34, -56, 90, -48, 61, -65, 79, 42, -1, 18, 5, 79, -103, -92, -104, -36, 1, -19, 32, -36, 13, 55, 59, -76, 96, 98, 110, -71, 107, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 33, 3, -76, 83, 75, 68, -47, -38, 71, -28, -76, -91, 4, -94, 16, 64, 26, 88, 63, -122, 4, 104, -34, -57, 102, -11, 7, 37, 26, 5, 117, -108, -26, -126, -30, -92, -73, -58, 88, 47, 78, -125, 118, 104, 80, 78, -78, -12, -22, -89, -106, -23, 8, -28, 0, 0, 0, 0, 0, 0, 0, 0, 59, -102, -54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 88, 50, -32, 39, 20, -102, -43, -33, -20, -107, 115, 43, -114, 45, -54, 80, 24, 102, -73, 108, -83, 9, -114, -126, -53, 122, -2, 36, -41, -73, -65, -111};
		Block b= Block.fromBytes(hh);
		System.out.println(b.height());
	}
}
