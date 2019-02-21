package com.uchain.networkmanager.message;

public enum MessageType {
	Version(0), BlockProduced(1), GetBlocks(2), Block(3), Blocks(4), Inventory(5),Getdata(6), Transactions(7), RPCCommand(8);
	private int value;

	private MessageType(int value) {
		this.value = value;
	}

	public static int getMessageTypeByType(MessageType messageType) {
		for (MessageType c : MessageType.values()) {
			if (c.value == messageType.value) {
				return c.value;
			}
		}
		return 100;
	}

	public static String getMessageTypeStringByType(MessageType messageType) {
		for (MessageType c : MessageType.values()) {
			if (c.value == messageType.value) {
				return c.toString();
			}
		}
		return "";
	}
	
	public static MessageType getMessageTypeByValue(byte value) {
        for (MessageType c : MessageType.values()) {
            if ((byte)c.value == value) {
                return c;
            }
        }
        return null;
    }
}
