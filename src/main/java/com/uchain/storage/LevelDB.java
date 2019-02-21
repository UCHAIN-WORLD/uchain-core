package com.uchain.storage;

import org.iq80.leveldb.DB;

public class LevelDB implements LowLevelDB {

    private DB db;

    public LevelDB(DB db){
        this.db = db;
    }
    @Override
    public byte[] get(byte[] key) {
        return db.get(key);
    }

    @Override
    public void set(byte[] key, byte[] value) {
        db.put(key,value);
    }

    @Override
    public void delete(byte[] key) {
        db.delete(key);
    }

    @Override
    public LowLevelDBIterator iterator() {
        return new LevelDBIterator(db.iterator());
    }

    @Override
    public void batchWrite(LowLevelWriteBatch action) {
        LevelDBWriteBatch update = new LevelDBWriteBatch(db.createWriteBatch());
        try {
            //action(update)
            db.write(update.getBatch());
        } finally {
            update.close();
        }
    }
}
