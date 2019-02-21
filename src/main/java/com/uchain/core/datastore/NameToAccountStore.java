package com.uchain.core.datastore;

import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.cryptohash.UInt160;
import com.uchain.storage.LevelDbStorage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NameToAccountStore extends StoreBase<String, UInt160> {
	private LevelDbStorage db;
	private int cacheCapacity;
	private byte[] prefixBytes;
	private Converter<String> keyConverter;
	private Converter<UInt160> valConverter;

	public NameToAccountStore(LevelDbStorage db, int cacheCapacity, byte[] prefixBytes, Converter<String> keyConverter,
			Converter<UInt160> valConverter) {
		super(db, cacheCapacity, prefixBytes, keyConverter, valConverter);
		this.db = db;
		this.cacheCapacity = cacheCapacity;
		this.prefixBytes = prefixBytes;
		this.keyConverter = keyConverter;
		this.valConverter = valConverter;
	}
}
