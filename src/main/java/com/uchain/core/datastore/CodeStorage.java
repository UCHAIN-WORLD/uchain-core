package com.uchain.core.datastore;

import com.uchain.core.Code;
import com.uchain.core.Contract;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.crypto.UInt160;
import com.uchain.storage.LevelDbStorage;
import com.uchain.storage.Storage;

public class CodeStorage extends StoreBase<byte[], Code>{
    private LevelDbStorage db;
    private int cacheCapacity;
    private byte[] prefixBytes;
    private Converter<byte[]> keyConverter;
    private Converter<Code> valConverter;

    public CodeStorage(Storage db, int cacheCapacity, byte[] prefixBytes, Converter<byte[]> keyConverter,
                       Converter<Code> valConverter) {
        super((LevelDbStorage)db, cacheCapacity, prefixBytes, keyConverter, valConverter);
        this.db = (LevelDbStorage)db;
        this.cacheCapacity = cacheCapacity;
        this.prefixBytes = prefixBytes;
        this.keyConverter = keyConverter;
        this.valConverter = valConverter;
    }
}
