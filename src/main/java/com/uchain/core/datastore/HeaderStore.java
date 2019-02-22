package com.uchain.core.datastore;

import com.uchain.core.block.BlockHeader;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.cryptohash.UInt256;
import com.uchain.storage.LevelDbStorage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeaderStore extends StoreBase<UInt256, BlockHeader>{
	private LevelDbStorage db;
	private int cacheCapacity;
	private byte[] prefixBytes;
	private Converter<UInt256> keyConverter;
	private Converter<BlockHeader> valConverter;
	
	public HeaderStore(LevelDbStorage db, int cacheCapacity, byte[] prefixBytes, Converter<UInt256> keyConverter,
			Converter<BlockHeader> valConverter) {
		super(db, cacheCapacity,prefixBytes,keyConverter, valConverter);
		this.db = db;
		this.cacheCapacity = cacheCapacity;
		this.prefixBytes = prefixBytes;
		this.keyConverter = keyConverter;
		this.valConverter = valConverter;
	}
}
