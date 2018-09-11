package com.uchain.core.datastore.keyvalue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import com.uchain.common.Serializabler;
import com.uchain.crypto.UInt160;

import lombok.val;

public class UInt160Value implements Converter<UInt160>{

	@Override
	public byte[] toBytes(UInt160 key) {
		return Serializabler.toBytes(key);
	}

	@Override
	public UInt160 fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		return UInt160.deserialize(is);
	}

}
