package com.uchain.core.datastore;

import com.uchain.storage.Batch;
import com.uchain.storage.LevelDbStorage;
import com.uchain.storage.Storage;

public class TrackingRoot extends Tracking{

    Storage db;

    public TrackingRoot(Storage db){
        this.db = db;
    }

    @Override
    public byte[] get(byte[] key) {
        return ((LevelDbStorage)db).get(key);
    }

    @Override
    public boolean set(byte[] key, byte[] value, Batch batch){
        return db.set(key, value, batch);
    }

    @Override
    public boolean delete(byte[] key, Batch batch){
        return db.delete(key, batch);
    }

    @Override
    public void commit(){
        db.commit();
    }
}
