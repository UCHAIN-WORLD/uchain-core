package com.uchain.core.datastore.keyvalue;

import com.uchain.crypto.UInt160;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class UInt160Key implements Converter<UInt160>{

	@Override
	public byte[] toBytes(UInt160 key) {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		serializer(key,os);
		return bs.toByteArray();
	}

	@Override
	public UInt160 fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		return UInt160.deserialize(is);
	}

	@Override
	public UInt160 deserializer(DataInputStream is) {
		return UInt160.deserialize(is);
	}

	@Override
	public void serializer(UInt160 key, DataOutputStream os) {
		key.serialize(os);
	}

}
