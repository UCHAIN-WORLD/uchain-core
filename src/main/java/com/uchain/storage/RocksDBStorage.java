package com.uchain.storage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.val;
import org.rocksdb.*;
import scala.NotImplementedError;

public class RocksDBStorage implements Storage<byte[],byte[]> {

    private RocksDB db;
    static {
        RocksDB.loadLibrary();
    }

    public RocksDBStorage(RocksDB db){
        this.db = db;
    }

    @Override
    public boolean containsKey(byte[] bytes) {
        return false;
    }

    @Override
    public byte[] get(byte[] key) {
        try{
            ReadOptions opt = new ReadOptions().setFillCache(true);
            byte[] value = db.get(opt,key);
            if (value == null) {
                return null;
            } else {
                return value;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean set(byte[] bytes, byte[] bytes2, Batch batch) {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public boolean delete(byte[] bytes, Batch batch) {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public void newSession() {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public void commit(Integer revision) {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public void commit() {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public void rollBack() {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public void close() {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public List<Map.Entry<byte[], byte[]>> find(byte[] prefix) {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public Batch batchWrite() {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public Map.Entry<byte[], byte[]> last() throws IOException {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public Integer revision() {
        throw new NotImplementedError("This Exception!");
    }

    @Override
    public List<Integer> uncommitted() {
        throw new NotImplementedError("This Exception!");
    }

    public static RocksDBStorage open(String path) {
        return open(path,true);
    }
    public static RocksDBStorage open(String path, Boolean createIfMissing){
        try {
            val options = new Options();
            options.setCreateIfMissing(createIfMissing);
            val db = RocksDB.open(options, path);
            return new RocksDBStorage(db);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }
}
