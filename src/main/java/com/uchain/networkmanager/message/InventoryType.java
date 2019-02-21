package com.uchain.networkmanager.message;

public enum InventoryType {
	Block(0x00), Tx(0x01);
	private int value;

	private InventoryType(int value) {
		this.value = value;
	}

	public static int getInventoryTypeByType(InventoryType inventoryType) {
		for (InventoryType c : InventoryType.values()) {
			if (c.value == inventoryType.value) {
				return c.value;
			}
		}
		return 100;
	}

	public static String getInventoryTypeStringByType(InventoryType inventoryType) {
		for (InventoryType c : InventoryType.values()) {
			if (c.value == inventoryType.value) {
				return c.toString();
			}
		}
		return "";
	}
	
	public static InventoryType getInventoryTypeByValue(byte value) {
        for (InventoryType c : InventoryType.values()) {
            if ((byte)c.value == value) {
                return c;
            }
        }
        return null;
    }
}
