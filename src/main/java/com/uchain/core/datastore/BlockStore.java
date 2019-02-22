package com.uchain.core.datastore;

import com.uchain.core.block.Block;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.cryptohash.UInt256;
import com.uchain.storage.LevelDbStorage;

public class BlockStore extends StoreBase<UInt256, Block>{
    private LevelDbStorage db;
    private int cacheCapacity;
    private byte[] prefixBytes;
    private Converter<UInt256> keyConverter;
    private Converter<Block> valConverter;

    public BlockStore(LevelDbStorage db, int cacheCapacity, byte[] prefixBytes, Converter<UInt256> keyConverter,
               Converter<Block> valConverter){
        super(db, cacheCapacity,prefixBytes,keyConverter, valConverter);
        this.db = db;
        this.cacheCapacity = cacheCapacity;
        this.prefixBytes = prefixBytes;
        this.keyConverter = keyConverter;
        this.valConverter = valConverter;
    }

}
