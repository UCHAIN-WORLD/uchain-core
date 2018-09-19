package com.uchain.core.datastore.keyvalue;

import com.uchain.common.Serializabler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

	@Override
	public String deserializer(DataInputStream is) {
		try {
			return new String(Serializabler.readByteArray(is), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void serializer(String key, DataOutputStream os) {
		try {
			Serializabler.writeByteArray(os,key.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
