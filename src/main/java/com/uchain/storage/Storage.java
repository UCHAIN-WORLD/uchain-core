package com.uchain.storage;

import java.util.Map;

public interface Storage<Key, Value> {
	boolean set(byte[] key, byte[] value);

	byte[] get(byte[] key);
	
	void delete(byte[] key);
	
	void scan();
	//
	// def find(prefix: Array[Byte], func: (Key, Value) => Unit): Unit
	//
	void commit();
	
	void close();
	
	boolean containsKey(Key key);
}
