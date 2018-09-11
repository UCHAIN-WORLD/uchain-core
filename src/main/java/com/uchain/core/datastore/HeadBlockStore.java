package com.uchain.core.datastore;

import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.core.datastore.keyvalue.HeadBlock;
import com.uchain.storage.LevelDbStorage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadBlockStore extends StateStore<HeadBlock>{
	private LevelDbStorage db;
	private byte[] prefixBytes;
	private Converter<HeadBlock> valConverter;
	public HeadBlockStore(LevelDbStorage db, byte[] prefixBytes, Converter<HeadBlock> valConverter) {
		super(db, prefixBytes,valConverter);
		this.db = db;
		this.prefixBytes = prefixBytes;
		this.valConverter = valConverter;
	}
}
