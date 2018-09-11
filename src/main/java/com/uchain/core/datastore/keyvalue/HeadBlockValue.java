package com.uchain.core.datastore.keyvalue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.uchain.common.Serializabler;

import lombok.val;

public class HeadBlockValue implements Converter<HeadBlock>{

	@Override
	public byte[] toBytes(HeadBlock key) {
		return Serializabler.toBytes(key);
	}

	@Override
	public HeadBlock fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		try {
			return HeadBlock.deserialize(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
