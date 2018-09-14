package com.uchain.core.datastore.keyvalue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import com.uchain.common.Serializabler;
import com.uchain.crypto.UInt256;

import lombok.val;

public class UInt256Key implements Converter<UInt256> {

	@Override
	public byte[] toBytes(UInt256 key) {
		return Serializabler.toBytes(key);
	}

	@Override
	public UInt256 fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		return UInt256.deserialize(is);
	}

}
