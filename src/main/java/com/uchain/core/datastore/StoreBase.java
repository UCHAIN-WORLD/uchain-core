package com.uchain.core.datastore;

import com.uchain.common.Cache;
import com.uchain.common.LRUCache;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.storage.Batch;
import com.uchain.storage.LevelDbStorage;
import lombok.val;

abstract class StoreBase<K, V> {

    private LevelDbStorage db;
    private int cacheCapacity;
    private byte[] prefixBytes;
    private Cache<K, V> cache;
    private Converter keyConverter;

    private Converter valConverter;

    public StoreBase(LevelDbStorage db, int cacheCapacity, byte[] prefixBytes, Converter keyConverter,
                     Converter valConverter) {
        this.db = db;
        this.cacheCapacity = cacheCapacity;
        this.prefixBytes = prefixBytes;
        this.keyConverter = keyConverter;
        this.valConverter = valConverter;
        this.cache = new LRUCache(cacheCapacity);
    }

    public byte[] getPrefixBytes() {
        return prefixBytes;
    }

    public LevelDbStorage getDb() {
        return db;
    }

    public boolean contains(K key) {
        return cache.contains(key) || backContains(key);
    }

    public V get(K key) {
        V item = cache.get(key);
        if (item == null) {
            item = getFromBackStore(key);
            if (item == null) {
                return null;
            } else {
                return item;
            }
        } else {
            return item;
        }
    }

    public V get() {
        if (cache == null) {
            val bytes = db.get(prefixBytes);
            if (!(bytes == null)) {
                return (V) valConverter.fromBytes(bytes);
            } else {
                return null;
            }
        }
        return null;
    }

    public boolean set(K key, V value, Batch batch) {
        if (setBackStore(key, value, batch)) {
            return true;
        } else {
            return false;
        }
    }

    public void delete(K key, Batch batch) {
        deleteBackStore(key, batch);
    }

    public byte[] genKey(K key) {
        byte[] keyBytes = keyConverter.toBytes(key);
        byte[] keyBytesAddPrefix = new byte[prefixBytes.length + keyBytes.length];
        for (int i = 0; i < prefixBytes.length; i++) {
            keyBytesAddPrefix[i] = prefixBytes[i];
        }

        for (int j = 0; j < keyBytes.length; j++) {
            keyBytesAddPrefix[prefixBytes.length + j] = keyBytes[j];
        }
        return keyBytesAddPrefix;
    }

    private boolean backContains(K key) {
        return db.containsKey(genKey(key));
    }

    public V getFromBackStore(K key) {
        byte[] value = db.get(genKey(key));
        if (value != null) {
            return (V) valConverter.fromBytes(value);
        } else {
            return null;
        }
    }

    public boolean setBackStore(K key, V value, Batch batch) {
        return db.set(genKey(key), valConverter.toBytes(value), batch);
    }

    public void deleteBackStore(K key, Batch batch) {
        db.delete(genKey(key), batch);
    }
}
