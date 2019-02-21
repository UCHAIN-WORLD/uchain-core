package com.uchain.core.datastore.keyvalue;

import java.io.*;

import com.uchain.common.Serializabler;
import com.uchain.core.BlockHeader;

import com.uchain.crypto.UInt256;
import lombok.val;

public class BlockHeaderValue implements Converter<BlockHeader>{

	@Override
	public byte[] toBytes(BlockHeader key) {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		serializer(key,os);
		return bs.toByteArray();
	}

	@Override
	public BlockHeader fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		try {
			return BlockHeader.deserialize(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BlockHeader deserializer(DataInputStream is) {
		try {
			return BlockHeader.deserialize(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void serializer(BlockHeader key, DataOutputStream os) {
		key.serialize(os);
	}

}
