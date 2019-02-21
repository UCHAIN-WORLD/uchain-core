package com.uchain.core.datastore;

import com.uchain.core.BlockHeader;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.storage.LevelDbStorage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadBlockStore extends StateStore<BlockHeader> {
    private LevelDbStorage db;
    private byte[] prefixBytes;
    private Converter<BlockHeader> valConverter;

    public HeadBlockStore(LevelDbStorage db, byte[] prefixBytes, Converter<BlockHeader> valConverter) {
        super(db, prefixBytes, valConverter);
        this.db = db;
        this.prefixBytes = prefixBytes;
        this.valConverter = valConverter;
    }
}
