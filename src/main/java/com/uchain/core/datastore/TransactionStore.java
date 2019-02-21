package com.uchain.core.datastore;

import com.uchain.core.Transaction;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.cryptohash.UInt256;
import com.uchain.storage.LevelDbStorage;

public class TransactionStore extends StoreBase<UInt256, Transaction>{
	private LevelDbStorage db;
	private int cacheCapacity;
	private byte[] prefixBytes;
	private Converter<UInt256> keyConverter;
	private Converter<Transaction> valConverter;

	public TransactionStore(LevelDbStorage db, int cacheCapacity, byte[] prefixBytes, Converter<UInt256> keyConverter,
			Converter<Transaction> valConverter) {
		super(db, cacheCapacity, prefixBytes, keyConverter, valConverter);
		this.db = db;
		this.cacheCapacity = cacheCapacity;
		this.prefixBytes = prefixBytes;
		this.keyConverter = keyConverter;
		this.valConverter = valConverter;
	}
}
