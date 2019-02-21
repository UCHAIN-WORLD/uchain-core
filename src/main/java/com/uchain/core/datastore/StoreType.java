package com.uchain.core.datastore;

public enum StoreType {
	Data(0x00), Index(0x01), State(0x02);
	
	private int value;

	private StoreType(int value) {
		this.value = value;
	}
	
	public static int getStoreType(StoreType storeType) {
        for (StoreType c : StoreType.values()) {
            if (c.value == storeType.value) {
                return c.value;
            }
        }
        return 100;
    }
}