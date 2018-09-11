package com.uchain.core.datastore.keyvalue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.uchain.common.Serializabler;
import com.uchain.core.BlockHeader;

import lombok.val;

public class BlockHeaderValue implements Converter<BlockHeader>{

	@Override
	public byte[] toBytes(BlockHeader key) {
		return Serializabler.toBytes(key);
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

}
