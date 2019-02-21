package com.uchain.core.datastore.keyvalue;

import com.uchain.crypto.UInt256;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class UInt256Key implements Converter<UInt256> {

	@Override
	public byte[] toBytes(UInt256 key) {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		serializer(key,os);
		return bs.toByteArray();
	}

	@Override
	public UInt256 fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		return UInt256.deserialize(is);
	}

	@Override
	public UInt256 deserializer(DataInputStream is) {
		return UInt256.deserialize(is);
	}

	@Override
	public void serializer(UInt256 key, DataOutputStream os) {
		key.serialize(os);
	}

}
