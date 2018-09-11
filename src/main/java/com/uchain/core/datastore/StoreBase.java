package com.uchain.core.datastore;

import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.WriteBatch;

import com.uchain.common.Cache;
import com.uchain.common.LRUCache;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.crypto.UIntBase;
import com.uchain.storage.LevelDbStorage;

import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

abstract class StoreBase<K, V> {

	private static final Logger log = LoggerFactory.getLogger(StoreBase.class);
	private LevelDbStorage db;
	private int cacheCapacity;
	private byte[] prefixBytes;
    private Cache<K ,V> cache;
	private Converter keyConverter;

	private Converter valConverter;
	
	public StoreBase(LevelDbStorage db, int cacheCapacity, byte[] prefixBytes, Converter keyConverter,
			Converter valConverter) {
		this.db = db;
		this.cacheCapacity = cacheCapacity;
		this.prefixBytes = prefixBytes;
		this.keyConverter = keyConverter;
		this.valConverter = valConverter;
		this.cache=new LRUCache(cacheCapacity);
	}


	
//	def foreach(func: (K, V) => Unit): Unit = {
//		    db.find(prefixBytes, (k, v) => {
//		      val kData = k.drop(prefixBytes.length)
//		      func(keyConverter.fromBytes(kData),
//		        valConverter.fromBytes(v))
//		    })
//		  }
public void foreachForDelete(WriteBatch batch){
	DBIterator iterator = db.db.iterator();
	try {
		iterator.seek(prefixBytes);
		while (iterator.hasNext()) {
			val peekNextEntry = iterator.peekNext();
			val getPrefixBytes = new byte[prefixBytes.length];
			System.arraycopy(peekNextEntry.getKey(), 0, getPrefixBytes,0,prefixBytes.length);
			val keyIsSame = (peekNextEntry.getKey().length < prefixBytes.length || !new String(prefixBytes).equals(new String(getPrefixBytes)));
			val keys = keyConverter.fromBytes(getPrefixBytes);
			delete((K) keys, batch);
			if(keyIsSame) iterator.next();
			else break;
		}
	}
	catch (Exception e) {
		log.error("find", e);
	} finally {
		try {
			iterator.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
    public boolean contains(K key) {
    	if(cache.contains(key)) {
    		return true;
    	}else {
    		if(getFromBackStore(key)!=null) {
    			return true;
    		}else {
    			return false;
    		}
    	}
    }
	
    public V get(K key) {
		V item = cache.get(key);
    	if(item == null) {
    		item = getFromBackStore(key);
    		if(item == null) {
    			return null;
    		}else {
    			cache.set(key, item);
    			return item;
    		}
    	}else {
    		return item;
    	}
    }
    
    public boolean set(K key,V value,WriteBatch writeBatch) {
    	if (setBackStore(key, value, writeBatch)) {
    		cache.set(key, value);
    		return true;
    	}else {
    		return false;
    	}
    }
    
    public void delete(K key,WriteBatch writeBatch) {
    	deleteBackStore(key, writeBatch);
	    cache.delete(key);
    }
   
    @SuppressWarnings("unchecked")
    public byte[] genKey(K key) {
//		byte[] bt2 = keyConverter.toBytes(key);
//    	int bt1Length = prefixBytes.length;
//    	int bt2Length = keyConverter.toBytes(key).length;
//    	byte[] bt3 = new byte[bt1Length+bt2Length];  
//        System.arraycopy(prefixBytes, 0, bt3, 0, bt1Length);  
//        System.arraycopy(bt2, 0, bt3, bt1Length, bt2Length);  
//        return bt3;  
    	return null;
    }

    public V getFromBackStore(K key) {
    	val opt = new ReadOptions().fillCache(false);
    	byte[] value = db.get(genKey(key), opt);
    	if(value!=null) {
    		return (V) valConverter.fromBytes(value);
    	}else {
    		return null;
    	}
    }
    
    public boolean setBackStore(K key,V value,WriteBatch batch) {
//    	if (batch != null) {
//  	      batch.put(genKey(key), valConverter.toBytes(value));
//  	      return true;
//  	    } else {
//  	      return db.set(genKey(key), valConverter.toBytes(value));
//  	    }
    	return false;
    }
   
    public void deleteBackStore(K key,WriteBatch batch) {
    	if (batch != null) {
  	      batch.delete(genKey(key));
  	    } else {
  	      db.delete(genKey(key));
  	    }
    }
}
