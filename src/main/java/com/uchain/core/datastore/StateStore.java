package com.uchain.core.datastore;

import org.iq80.leveldb.WriteBatch;

import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.storage.LevelDbStorage;

import lombok.val;

abstract class StateStore<T> {
	private LevelDbStorage db;
	private T cached = null;
	private byte[] prefixBytes;
	private Converter valConverter;

	public StateStore(LevelDbStorage db, byte[] prefixBytes,Converter valConverter) {
		this.db = db;
		this.prefixBytes = prefixBytes;
		this.valConverter = valConverter;
	}
	
	public T get() {
		if (cached == null) {
			val bytes = db.get(prefixBytes);
			if (!(bytes == null)) {
				cached = (T) valConverter.fromBytes(bytes);
			} else {
				return null;
			}
		} 
		return cached;
	}

	public boolean set(T value,WriteBatch writeBatch) {
    	if (value == null) {
    		return false;
    	}else {
    		if (writeBatch != null) {
    			writeBatch.put(prefixBytes, valConverter.toBytes(value));
		        cached = value;
		        return true;
		      } else {
		        if (db.set(prefixBytes, valConverter.toBytes(value))) {
		          cached = value;
		          return true;
		        } else {
		        	return false;
		        }
		      }
    	}
    }

	public void delete(WriteBatch writeBatch) {
		if (writeBatch != null) {
			writeBatch.delete(prefixBytes);
		} else {
			db.delete(prefixBytes);
		}
		cached = null;
	}
}
