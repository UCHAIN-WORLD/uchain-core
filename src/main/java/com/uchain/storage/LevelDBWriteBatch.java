package com.uchain.storage;

import lombok.Getter;
import lombok.Setter;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;

@Getter
@Setter
public class LevelDBWriteBatch implements LowLevelWriteBatch {

    private WriteBatch batch;

    public LevelDBWriteBatch(WriteBatch batch){
        this.batch = batch;
    }
    @Override
    public void set(byte[] key, byte[] value) {
        batch.put(key, value);
    }

    @Override
    public void delete(byte[] key) {
        batch.delete(key);
    }

    @Override
    public void close() {
        try {
            batch.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
