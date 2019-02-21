package com.uchain.storage;
/* *************************************************************
 * Copyright  2018 ------------ All rights reserved.
 *
 * FileName: RocksDatabase
 *
 * @Author: //.bu------------ 2018/10/23 18:15
 *
 * @Version: 1.0
 * *************************************************************/

import lombok.val;
import org.rocksdb.RocksDB;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

public class RocksDatabase implements LowLevelDB {

    private RocksDB db;

    public RocksDatabase(RocksDB db){
        this.db = db;
    }

    @Override
    public byte[] get(byte[] key) {
        try {
            return db.get(key);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void set(byte[] key, byte[] value) {
        try {
            db.put(key,value);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void delete(byte[] key) {
        try {
            db.delete(key);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public LowLevelDBIterator iterator() {
        return new RocksDBIterator(db.newIterator());
    }

    @Override
    public void batchWrite(LowLevelWriteBatch action) {
        val update = new RocksDBWriteBatch(new WriteBatch());
        try {
            //action(update)
            db.write(new WriteOptions(),update.getBatch());
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            update.close();
        }
    }
}
