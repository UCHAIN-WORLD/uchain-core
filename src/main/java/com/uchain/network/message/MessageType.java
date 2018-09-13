package com.uchain.network.message;

public enum MessageType {
	Version(0), BlockProduced(1), GetBlocks(2), Block(3), Inventory(4), Getdata(5);
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
