package com.uchain.core.datastore.keyvalue;

import lombok.val;

import java.io.*;

public class IntKey implements Converter<Integer>{

	@Override
	public byte[] toBytes(Integer key) {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		serializer(key,os);
		return bs.toByteArray();
	}

	@Override
	public Integer fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		return deserializer(is);
	}

	@Override
	public Integer deserializer(DataInputStream is) {
        int read = 0;
        try {
            read = is.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return read;
	}

	@Override
	public void serializer(Integer key, DataOutputStream os) {
        try {
            os.write(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
