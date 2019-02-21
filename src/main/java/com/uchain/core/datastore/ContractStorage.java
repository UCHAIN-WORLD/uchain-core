package com.uchain.core.datastore;

import com.uchain.core.Contract;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import com.uchain.storage.LevelDbStorage;
import com.uchain.storage.Storage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractStorage extends StoreBase<byte[], Contract>{
    private LevelDbStorage db;
    private int cacheCapacity;
    private byte[] prefixBytes;
    private Converter<byte[]> keyConverter;
    private Converter<Contract> valConverter;

    public ContractStorage(Storage db, int cacheCapacity, byte[] prefixBytes, Converter<byte[]> keyConverter,
                           Converter<Contract> valConverter) {
        super((LevelDbStorage)db, cacheCapacity, prefixBytes, keyConverter, valConverter);
        this.db = (LevelDbStorage)db;
        this.cacheCapacity = cacheCapacity;
        this.prefixBytes = prefixBytes;
        this.keyConverter = keyConverter;
        this.valConverter = valConverter;
    }
}
