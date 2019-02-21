package com.uchain.core.datastore;

import com.uchain.storage.Batch;
import lombok.val;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.math.BigInteger;

public class RollSession extends Session{

    private DB db;

    private byte[] prefix;

    private int _revision;

    private byte[] sessionId;

    private SessionItem item = new SessionItem();

    private boolean closed = false;

    public RollSession(DB db,byte[] prefix,Integer _revision){
        this.db = db;
        this.prefix = prefix;
        this._revision = _revision;
        this.sessionId = Session.byteMergerAll(prefix,new BigInteger(_revision.toString()).toByteArray());
        //init();
    }

    public void init(byte[] data){
        assert(!closed);
        item.fill(data);
    }

    public void init(WriteBatch batch){
        assert(!closed);
            batch.put(sessionId,item.toBytes());
            //执行 action(batch) 原子操作
    }

    public void close(){
        assert(!closed);
        db.delete(sessionId);
        closed = true;
    }

    public int get_revision(){
        return _revision;
    }
    /*private void init(){
        byte[] session = db.get(this.sessionId);
        if (session != null) {
            db.set(sessionId,item.toBytes());
        } else {
            item.fill(session);
        }
    }*/

    public void rollBack() throws IOException {
        assert(!closed);
        val batch = db.createWriteBatch();
        try {
            item.getInsert().forEach((k, v) -> batch.delete(k.getBytes()));
            item.getUpdate().forEach((k, v) -> batch.put(k.getBytes(), v));
            item.getDelete().forEach((k, v) -> batch.put(k.getBytes(), v));
            batch.put(prefix, new BigInteger(_revision+"").toByteArray());
            batch.delete(sessionId);
            db.write(batch);
        }finally {
            batch.close();
        }
    }
    @Override
    public Batch onSet(byte[] key, byte[] v, Batch batch){
        assert(!closed);

        Batch newBatch = originOrNew(batch);
        newBatch.put(key,v);

        Boolean modified = true;
        ByteArrayKey k = new ByteArrayKey(key);

        if(item.getInsert().containsKey(k) || item.getUpdate().containsKey(k)){
            modified = false;
        }else if (item.getDelete().containsKey(k)) {
            item.getUpdate().put(k, item.getDelete().remove(k));
            item.getDelete().remove(k);
        }else{
            val old= db.get(k.getBytes());
            if(null != old ){
                item.getUpdate().put(k,old);
            }else{
                item.getInsert().put(k,v);
            }

        }

        if (modified) {
            newBatch.put(sessionId, item.toBytes());
        }
        return newBatch;

    }
    @Override
    public Batch onDelete(byte[] key,Batch batch){
        assert (!closed);
        Batch newBatch = originOrNew(batch);
        newBatch.delete(key);

        Boolean modified = false;
        ByteArrayKey k = new ByteArrayKey(key);

        if(item.getInsert().containsKey(k)){
            item.getInsert().remove(k);
            modified = true;
        }else if (item.getUpdate().containsKey(k)){
            item.getDelete().put(k,item.getUpdate().put(k,item.getInsert().remove(k)));
            item.getUpdate().remove(k);
            modified = true;
        }else if(!item.getDelete().containsKey(k)){
            val old= db.get(k.getBytes());
            if(null != old ){
                item.getDelete().put(k,old);
                modified = true;
            }
        }

        if (modified) {
            newBatch.put(sessionId, item.toBytes());
        }
        return newBatch;
    }

}
