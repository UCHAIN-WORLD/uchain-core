package com.uchain.core.datastore;

import com.uchain.core.BlkTxMapping;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.crypto.UInt256;
import com.uchain.storage.LevelDbStorage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlkTxMappingStore extends StoreBase<UInt256, BlkTxMapping>{
	private LevelDbStorage db;
	private int cacheCapacity;
	private byte[] prefixBytes;
	private Converter<UInt256> keyConverter;
	private Converter<BlkTxMapping> valConverter;

	public BlkTxMappingStore(LevelDbStorage db, int cacheCapacity, byte[] prefixBytes, Converter<UInt256> keyConverter,
			Converter<BlkTxMapping> valConverter) {
		super(db, cacheCapacity,prefixBytes, keyConverter, valConverter);
		this.db = db;
		this.cacheCapacity = cacheCapacity;
		this.prefixBytes = prefixBytes;
		this.keyConverter = keyConverter;
		this.valConverter = valConverter;
	}
}
