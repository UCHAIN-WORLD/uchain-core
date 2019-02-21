package com.uchain.core.datastore;

public enum IndexType {
	BlockHeightToId(0x00),BlockIdToTxId(0x01),UTXO(0x02),NameToAccount(0x03);
	
	private int value;

	private IndexType(int value) {
		this.value = value;
	}

	public static int getIndexType(IndexType indexType) {
		for (IndexType c : IndexType.values()) {
			if (c.value == indexType.value) {
				return c.value;
			}
		}
		return 100;
	}
}
