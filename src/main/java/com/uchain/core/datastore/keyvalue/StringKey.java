package com.uchain.core.datastore.keyvalue;

import java.io.UnsupportedEncodingException;

public class StringKey implements Converter<String>{

	@Override
	public byte[] toBytes(String key) {
		try {
			return key.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String fromBytes(byte[] bytes) {
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
