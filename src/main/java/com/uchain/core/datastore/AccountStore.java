package com.uchain.core.datastore;

import com.uchain.core.Account;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.crypto.UInt160;
import com.uchain.storage.LevelDbStorage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountStore extends StoreBase<UInt160, Account> {
	private LevelDbStorage db;
	private int cacheCapacity;
	private byte[] prefixBytes;
	private Converter<UInt160> keyConverter;
	private Converter<Account> valConverter;

	public AccountStore(LevelDbStorage db, int cacheCapacity, byte[] prefixBytes, Converter<UInt160> keyConverter,
			Converter<Account> valConverter) {
		super(db, cacheCapacity, prefixBytes, keyConverter, valConverter);
		this.db = db;
		this.cacheCapacity = cacheCapacity;
		this.prefixBytes = prefixBytes;
		this.keyConverter = keyConverter;
		this.valConverter = valConverter;
	}
}
