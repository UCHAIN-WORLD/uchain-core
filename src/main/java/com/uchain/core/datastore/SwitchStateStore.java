package com.uchain.core.datastore;


import com.uchain.core.consensus.SwitchState;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.storage.LevelDbStorage;

public class SwitchStateStore extends StateStore<SwitchState> {

    private byte[] prefixBytes;
    private Converter valConverter;
    private LevelDbStorage db;

    public SwitchStateStore(LevelDbStorage db, byte[] prefixBytes, Converter valConverter) {
        super(db, prefixBytes, valConverter);
        this.db = db;
        this.prefixBytes = prefixBytes;
        this.valConverter = valConverter;
    }
}
