package com.uchain.core.datastore;

import com.uchain.core.Account;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.cryptohash.UInt160;
import com.uchain.storage.LevelDbStorage;

import com.uchain.storage.Storage;
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

	public AccountStore(Storage db, int cacheCapacity, byte[] prefixBytes, Converter<UInt160> keyConverter,
						Converter<Account> valConverter) {
		super((LevelDbStorage)db, cacheCapacity, prefixBytes, keyConverter, valConverter);
		this.db = (LevelDbStorage)db;
		this.cacheCapacity = cacheCapacity;
		this.prefixBytes = prefixBytes;
		this.keyConverter = keyConverter;
		this.valConverter = valConverter;
	}
}
