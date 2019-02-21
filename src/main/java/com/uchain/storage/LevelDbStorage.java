package com.uchain.storage;

import com.uchain.core.consensus.ThreeTuple;
import com.uchain.core.consensus.TwoTuple;
import com.uchain.core.datastore.SessionManger;
import com.uchain.core.datastore.keyvalue.Converter;
import lombok.val;
import org.iq80.leveldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

public class LevelDbStorage implements Storage<byte[], byte[]> {
	private static final Logger log = LoggerFactory.getLogger(LevelDbStorage.class);
	public DB db;
	private SessionManger sessionManger;

	public LevelDbStorage(){}

	public LevelDbStorage(DB db) {
		this.db = db;
        this.sessionManger = new SessionManger(db);
	}

	public SessionManger getSessionManger() {
		return sessionManger;
	}

    @Override
    public byte[] get(byte[] key) {
        ReadOptions opt = new ReadOptions().fillCache(true);
        byte[] value = db.get(key, opt);
        if (value == null) {
            return null;
        } else {
            return value;
        }
    }
    //batch is null
    public boolean set(byte[] key, byte[] value) {
        return set(key,value,null);
    }
	@Override
	public boolean set(byte[] key, byte[] value,Batch batch) {
        Batch newBatch = null;
        try {
            newBatch = sessionManger.beginSet(key, value, batch);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(newBatch != batch){
            return applyBatch(newBatch);
        } else {
	        return true;
        }
	}

    @Override
	public boolean delete(byte[] key, Batch batch){
        Batch newBatch = sessionManger.beginDelete(key, batch);
        if(newBatch != batch){
            return applyBatch(newBatch);
        } else {
            return true;
        }
    }

    @Override
    public void newSession(){
        try {
            sessionManger.newSession();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commit(Integer revision){
        sessionManger.commit(revision);
    }

    @Override
    public void commit() {
        sessionManger.commit();
    }

    @Override
    public void rollBack(){
        try {
            sessionManger.rollBack();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            db.close();
        } catch (IOException e) {
            log.error("db close failed", e);
        }
    }

    public synchronized boolean applyBatch(Batch batch){
        WriteBatch update = db.createWriteBatch();
        try {
            for (BatchItem batchOp : batch.getOps()) {
                if(batchOp.getClass() == PutOperationItem.class){
                    val key = ((PutOperationItem) batchOp).getKey();
                    val value = ((PutOperationItem) batchOp).getValue();
                    update.put(key, value);
                }

                if(batchOp.getClass() == DeleteOperationItem.class){
                    val key = ((DeleteOperationItem) batchOp).getKey();
                    update.delete(key);
                }
            }
            db.write(update);
            return true;
        }catch (Exception e){
            log.error("apply batch failed", e);
            return false;
        }finally {
            try {
                update.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Integer revision(){
	    return sessionManger.get_revision();
    }

    @Override
    public List<Integer> uncommitted(){
        return sessionManger.getRevisions();
    }

	public byte[] get(byte[] key, ReadOptions opt) {
		try {
			byte[] value = db.get(key, opt);
			if (value == null) {
				return null;
			} else {
				return value;
			}
		} catch (Exception e) {
			log.error("db get failed", e);
			return null;
		}
	}



	@Override
	public boolean containsKey(byte[] key){
		if(get(key) != null) return true;
		else return false;
	}



	public WriteBatch getBatchWrite() {
		WriteBatch batch = db.createWriteBatch();
//		try {
//			db.write(batch);
//		}finally {
//		      try {
//				batch.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		return batch;
	}

	public Batch batchWrite(){
	    val batch = new Batch();
	    return batch;
    }
	
	public void BatchWrite(WriteBatch batch) {
		try {
			db.write(batch);
		}finally {
		      try {
				batch.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Entry<byte[], byte[]> last() throws IOException{
	    val it = db.iterator();
	    try {
	        it.seekToLast();
	        if(it.hasNext()){
	            return it.peekNext();
            }
            else {
                return null;
            }
        }
        finally {
	        it.close();
        }
    }
	
	public List<Entry<byte[], byte[]>> scan() {
		DBIterator iterator = db.iterator();
		List<Entry<byte[], byte[]>> list = new ArrayList<Entry<byte[], byte[]>>();
		try {
			iterator.seekToFirst();
			while (iterator.hasNext()) {
				list.add(iterator.peekNext());
				iterator.next();
			}
		}
		catch (Exception e) {
			log.error("seek", e);
		} finally {
			try {
				iterator.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public List<Entry<byte[], byte[]>> find(byte[] prefix){
	    List<Entry<byte[], byte[]>> entryList = new ArrayList<>();
        DBIterator iterator = db.iterator();
        iterator.seek(prefix);
        while (iterator.hasNext()){
            Entry<byte[], byte[]> entry = iterator.peekNext();
            byte[] compareKeys = new byte[prefix.length];
            System.arraycopy(entry.getKey(), 0,compareKeys,0, prefix.length );
            if(entry.getKey().length < prefix.length || !Arrays.equals(prefix, compareKeys)){
                break;
            }
            else {
                entryList.add(entry);
                iterator.next();
            }
        }
        return entryList;
    }

	private ThreeTuple<Boolean, byte[], byte[]> findFunc(byte[] prefix, Map.Entry<byte[], byte[]> nextEntry){
        byte[] compareKeys = new byte[nextEntry.getKey().length];
        System.arraycopy(nextEntry.getKey(), 0,compareKeys,0, prefix.length );
        if(nextEntry.getKey().length < prefix.length || !Arrays.equals(prefix, compareKeys)) {
            return new ThreeTuple(false, nextEntry.getKey(), nextEntry.getValue());
        }
        else {
            return new ThreeTuple(true, nextEntry.getKey(), nextEntry.getValue());
        }
    }

//    private boolean func(byte[] key, byte[] value,Converter keyConverter, Converter valConverter){
//	    val kData = new byte[key.length];
//        System.arraycopy(key, 0,kData,0, key.length);
//        valConverter.fromBytes(value);
//    }


	public WriteBatch createBatchWrite(){
		return  db.createWriteBatch();
	}



//
//	@Override
//	public Map<String, String> scan() {
//		Map<String, String> linkedHashMap = Maps.newLinkedHashMap();
//		DBIterator iterator = db.iterator();
//		try {
//			iterator.seekToFirst();
//			while (iterator.hasNext()) {
//				Entry<byte[], byte[]> result = iterator.next();
//				linkedHashMap.put(new String(result.getKey()), new String(result.getValue()));
//			}
//		} catch (Exception e) {
//			log.error("seek", e);
//		} finally {
//			try {
//				iterator.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return linkedHashMap;
//	}
    public static LevelDbStorage open(String path){
        return open(path,true);
    }
    public static LevelDbStorage open(String path,Boolean createIfMissing){
        val options = new Options();
        options.createIfMissing(createIfMissing);
        try {
            DB db = factory.open(new File(path),options);
            return new LevelDbStorage(db);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

}
