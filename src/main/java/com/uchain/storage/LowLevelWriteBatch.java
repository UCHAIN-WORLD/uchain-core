package com.uchain.storage;

public interface LowLevelWriteBatch {
    void set(byte[] key, byte[] value);

    void delete(byte[] key);

    void close();
}
