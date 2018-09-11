package com.uchain.core.datastore.keyvalue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.uchain.common.Serializabler;

import lombok.val;

public class ProducerStatusValue implements Converter<ProducerStatus>{

	@Override
	public byte[] toBytes(ProducerStatus key) {
		return Serializabler.toBytes(key);
	}

	@Override
	public ProducerStatus fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		try {
			return ProducerStatus.deserialize(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
