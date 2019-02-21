package com.uchain.core.datastore.keyvalue;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface Converter<T> {
	byte[] toBytes(T key);

	T fromBytes(byte[] bytes);

	T deserializer(DataInputStream is);

	void serializer(T key, DataOutputStream os);
}
