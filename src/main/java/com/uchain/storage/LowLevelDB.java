package com.uchain.storage;

public interface LowLevelDB {
    byte[] get(byte[] key);

    void set(byte[] key,byte[] value);

    void delete(byte[] key);

    LowLevelDBIterator iterator();

    void batchWrite(LowLevelWriteBatch action);
}
