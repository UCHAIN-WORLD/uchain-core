package com.uchain.storage;

import com.google.common.collect.Maps;
import lombok.val;
import org.iq80.leveldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Array;
import scala.Byte;
import org.fusesource.leveldbjni.JniDBFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class LevelDbStorage implements Storage<byte[], byte[]> {
	private static final Logger log = LoggerFactory.getLogger(LevelDbStorage.class);
	public DB db;

	public LevelDbStorage(DB db) {
		this.db = db;
	}

	@Override
	public boolean set(byte[] key, byte[] value) {
		try {
			db.put(key, value);
			return true;
		} catch (Exception e) {
			log.error("db set failed", e);
			return false;
		}
	}

	@Override
	public byte[] get(byte[] key) {
		try {
			byte[] value = db.get(key);
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
	public void delete(byte[] key) {
		db.delete(key);
	}

	@Override
	public void commit() {

	}

	@Override
	public boolean containsKey(byte[] key){
		if(get(key) != null) return true;
		else return false;
	}

	@Override
	public void close() {
		try {
			db.close();
		} catch (IOException e) {
			log.error("db close failed", e);
		}
	}

	@Override
	public void scan() {
		DBIterator iterator = db.iterator();
		try {
			iterator.seekToFirst();
			while (iterator.hasNext()) {
				Entry<byte[], byte[]> result = iterator.next();
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
	}

	public void foreach(){

	}


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
}
