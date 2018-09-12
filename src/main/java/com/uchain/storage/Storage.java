package com.uchain.storage;

import java.util.List;
import java.util.Map.Entry;

public interface Storage<Key, Value> {
	boolean set(byte[] key, byte[] value);

	byte[] get(byte[] key);
	
	void delete(byte[] key);
	
	List<Entry<byte[], byte[]>> scan();
	//
	// def find(prefix: Array[Byte], func: (Key, Value) => Unit): Unit
	//
	void commit();
	
	void close();
	
	boolean containsKey(Key key);
}
