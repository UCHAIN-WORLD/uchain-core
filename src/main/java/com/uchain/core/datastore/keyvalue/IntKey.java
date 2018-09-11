package com.uchain.core.datastore.keyvalue;

import java.math.BigInteger;

public class IntKey implements Converter<Integer>{

	@Override
	public byte[] toBytes(Integer key) {
		return new BigInteger(String.valueOf(key)).toByteArray();
	}

	@Override
	public Integer fromBytes(byte[] bytes) {
		return (new BigInteger(bytes)).intValue();
	}

}
