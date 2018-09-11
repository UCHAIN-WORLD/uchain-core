package com.uchain.core.datastore;

public enum DataType {
	BlockHeader(0x00), Transaction(0x01), Account(0x02);

	private int value;

	private DataType(int value) {
		this.value = value;
	}

	public static int getDataType(DataType dataType) {
		for (DataType c : DataType.values()) {
			if (c.value == dataType.value) {
				return c.value;
			}
		}
		return 100;
	}
}
