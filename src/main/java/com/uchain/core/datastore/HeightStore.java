package com.uchain.core.datastore;

import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.crypto.UInt256;
import com.uchain.storage.LevelDbStorage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeightStore extends StoreBase<Integer, UInt256> {
	private LevelDbStorage db;
	private int cacheCapacity;
	private byte[] prefixBytes;
	private Converter<Integer> keyConverter;
	private Converter<UInt256> valConverter;

	public HeightStore(LevelDbStorage db, int cacheCapacity, byte[] prefixBytes, Converter<Integer> keyConverter,
			Converter<UInt256> valConverter) {
		super(db, cacheCapacity, prefixBytes, keyConverter, valConverter);
		this.db = db;
		this.cacheCapacity = cacheCapacity;
		this.prefixBytes = prefixBytes;
		this.keyConverter = keyConverter;
		this.valConverter = valConverter;
	}

}
