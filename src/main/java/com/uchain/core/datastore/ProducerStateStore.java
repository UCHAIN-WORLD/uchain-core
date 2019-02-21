package com.uchain.core.datastore;

import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.core.datastore.keyvalue.ProducerStatus;
import com.uchain.storage.LevelDbStorage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProducerStateStore extends StateStore<ProducerStatus> {

	private LevelDbStorage db;
	private byte[] prefixBytes;
	private Converter<ProducerStatus> valConverter;

	public ProducerStateStore(LevelDbStorage db, byte[] prefixBytes, Converter valConverter) {
		super(db, prefixBytes, valConverter);
		this.db = db;
		this.prefixBytes = prefixBytes;
		this.valConverter = valConverter;
	}

}
