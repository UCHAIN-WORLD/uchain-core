package com.uchain.core.datastore;



import java.util.Set;

public interface DbSource<V> extends BatchSource<byte[], V> {

    void setName(String name);

    String getName();

    void init();

    void init(DbSettings settings);

    boolean isAlive();

    void close();

    Set<byte[]> keys() throws RuntimeException;

    void reset();

    V prefixLookup(byte[] key, int prefixBytes);
}
