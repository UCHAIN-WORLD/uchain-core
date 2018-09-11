package com.uchain.core.datastore.keyvalue;

public interface Converter<T> {
	byte[] toBytes(T key);

	T fromBytes(byte[] bytes);
}
