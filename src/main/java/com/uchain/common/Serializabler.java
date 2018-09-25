package com.uchain.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.UInt256;

import lombok.val;

public class Serializabler {

	static ObjectMapper mapper;

	static{
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public static String  JsonMapperTo(Object object) throws IOException{
		String json = mapper.writeValueAsString(object);
		return json;
	}

	public static <T> T JsonMapperFrom(String content, Class<T> valueType) throws IOException{
		T object = mapper.readValue(content, valueType);
		return object;
	}

	public static byte[] toBytes(Serializable obj) {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		obj.serialize(os);
		return bs.toByteArray();
	}

	public static DataInputStream toInstance(byte[] bytes, Object object) throws IOException {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		return new DataInputStream(is);
	}

	public static List<DataInputStream> toInstanceList(byte[] bytes) throws IOException {
		List<DataInputStream> list = new ArrayList<>();
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		for (int i = 1; i < is.readInt(); i++) {
			list.add(new DataInputStream(is));
		}
		return list;
	}

	public static void writeByteArray(DataOutputStream os, byte[] bytes) throws IOException {
		os.writeInt(bytes.length);
		os.write(bytes);
	}

	public static void writeString(DataOutputStream os, String str) throws UnsupportedEncodingException, IOException {
		writeByteArray(os, str.getBytes("UTF-8"));
	}

	public static void  write(DataOutputStream os, Serializable value) {
		value.serialize(os);
	}

	public static <T extends Serializable> void  writeSeq(DataOutputStream os, List<T> t){
		try {
			os.writeInt(t.size());
			t.forEach(v -> {
				v.serialize(os);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeMap(DataOutputStream os, Map<UInt256, Fixed8> map) throws IOException {
		os.writeInt(map.size());
		map.forEach((key, value) -> {
			key.serialize(os);
			value.serialize(os);
		});
	}
	
	public static byte[] readByteArray(DataInputStream is) throws IOException {
		byte[] data = new byte[is.readInt()];
		Arrays.fill(data, (byte)0);
		is.read(data, 0, data.length);
		return data;
	}

	public static String readString(DataInputStream is) throws UnsupportedEncodingException, IOException {
		return new String(readByteArray(is), "UTF-8");
	}


}
