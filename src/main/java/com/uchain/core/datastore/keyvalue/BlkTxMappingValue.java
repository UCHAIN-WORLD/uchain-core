package com.uchain.core.datastore.keyvalue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.uchain.common.Serializabler;
import com.uchain.core.BlkTxMapping;
import lombok.val;

public class BlkTxMappingValue implements Converter<BlkTxMapping> {

	@Override
	public byte[] toBytes(BlkTxMapping key) {
		return Serializabler.toBytes(key);
	}

	@Override
	public BlkTxMapping fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		try {
			return BlkTxMapping.deserialize(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
