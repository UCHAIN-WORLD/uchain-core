package com.uchain.storage;


import lombok.Getter;
import lombok.Setter;
import org.rocksdb.WriteBatch;

@Getter
@Setter
public class RocksDBWriteBatch implements LowLevelWriteBatch {

    private WriteBatch batch;

    public RocksDBWriteBatch(WriteBatch batch){
        this.batch = batch;
    }

    @Override
    public void set(byte[] key, byte[] value) {
        batch.put(key, value);
    }

    @Override
    public void delete(byte[] key) {
        batch.remove(key);
    }

    @Override
    public void close() {
        try {
            batch.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
