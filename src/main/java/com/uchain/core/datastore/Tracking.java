package com.uchain.core.datastore;

import com.uchain.storage.Batch;
import com.uchain.storage.LevelDbStorage;
import com.uchain.storage.Storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tracking extends LevelDbStorage{

    public Tracking(){}

    Map<ByteArrayKey, CacheValue> cache = new HashMap<>();

    LevelDbStorage db;

    @Override
    public boolean containsKey(byte[] key){
        return cache.containsKey(new ByteArrayKey((key)));
    }

    @Override
    public byte[] get(byte[] key) {
       if(cache.containsKey(new ByteArrayKey(key))){
           return cache.get(new ByteArrayKey(key)).value;
       }
       return db.get(key);
    }

    @Override
    public boolean set(byte[] key, byte[] value, Batch batch){
        if(cache.get(new ByteArrayKey(key)) != null)  cache.get(new ByteArrayKey(key)).set(value);
        else cache.put(new ByteArrayKey(key), new CacheValue(value));
        return true;
    }

    @Override
    public boolean delete(byte[] key, Batch batch){
        if(cache.get(new ByteArrayKey(key)) != null) {
            cache.get(new ByteArrayKey(key)).delete();
        }
        else cache.put(new ByteArrayKey(key), new CacheValue());
        return true;
    }

//    boolean batchWrite();

    @Override
    public void newSession(){
        db.revision();
    }

    @Override
    public void commit(Integer revision){
        commit();
        db.commit(revision);
    }

    @Override
    public void commit(){
        cache.keySet().forEach(byteArrayKey -> {
            CacheValue cacheValue = cache.get(byteArrayKey);
            if(cacheValue.deleted) db.delete(byteArrayKey.getBytes(), null);
            else db.set(byteArrayKey.getBytes(), cacheValue.value);
        });
        cache.clear();
    }

    @Override
    public void rollBack(){
        cache.clear();
    }

    @Override
    public void close(){}

    @Override
    public List<Map.Entry<byte[], byte[]>> find(byte[] prefix){
        return db.find(prefix);
    }

    @Override
    public Batch batchWrite(){
        return db.batchWrite();
    }

    @Override
    public Map.Entry<byte[], byte[]> last() throws IOException{
        return db.last();
    }

    @Override
    public Integer revision(){
        return db.revision();
    }

    @Override
    public List<Integer> uncommitted(){
        return db.uncommitted();
    }

    public Tracking(LevelDbStorage db){
        this.db = db;
    }

}

class CacheValue{
    byte[] value;
    boolean deleted = false;

    CacheValue(byte[] value){
        this.value = value;
    }

    CacheValue(){
        this.value = null;
        this.deleted = true;
    }

    void set(byte[] value){
        this.value = value;
    }

    void delete(){
        this.value = null;
        this.deleted = true;
    }
}
