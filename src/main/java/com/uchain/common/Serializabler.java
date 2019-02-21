package com.uchain.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.uchain.cryptohash.*;
import com.uchain.util.Utils;
import lombok.val;

import java.io.*;
import java.util.*;

public class Serializabler {

	static ObjectMapper mapper;

	static{
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
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
        Utils.writeVarint(bytes.length,os);
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
		Utils.writeVarint(map.size(),os);
		map.forEach((key, value) -> {
			key.serialize(os);
			value.serialize(os);
		});
	}

	public static <K extends Serializable,V extends Serializable> void writeMap(DataOutputStream os, Map<K,V> map,Boolean flag) throws IOException {
		Utils.writeVarint(map.size(),os);
		map.forEach((key, value) -> {
			key.serialize(os);
			value.serialize(os);
		});
	}
	
	public synchronized static byte[] readByteArray(DataInputStream is) throws IOException {
		int length = Utils.readVarInt(is).intValue();
		byte[] data = new byte[length];
		Arrays.fill(data, (byte)0);
		is.read(data, 0, data.length);
		return data;
	}

	public static String readString(DataInputStream is) throws UnsupportedEncodingException, IOException {
		return new String(readByteArray(is), "UTF-8");
	}

	public static Map<UInt256, Fixed8> readMap(DataInputStream is,boolean flag) throws Exception{
		Map<UInt256, Fixed8> byteMap = new HashMap<>();
		val value = Utils.readVarInt(is);
		for(int i = 0; i< value; i++){
			byteMap.put(UInt256.deserialize(is), Fixed8.deserialize(is));
		}
		return byteMap;
	}

	public static Map<byte[], byte[]> readMap(DataInputStream is) throws Exception{
		Map<byte[], byte[]> byteMap = new HashMap<>();
		val value = Utils.readVarInt(is);
		for(int i = 1; i<= value; i++){
			byteMap.put(readByteArray(is), readByteArray(is));
		}
		return byteMap;
	}

	public static <T extends Serializable> T readObj(DataInputStream is,/*T t*/String str){
		/*if(t instanceof UInt256){
			return (T)UInt256.deserialize(is);
		}
		else if(t instanceof UInt160){
			return (T)UInt160.deserialize(is);
		}*/

		if("256" == str){
			return (T)UInt256.deserialize(is);
		}
		else if("160" == str){
			return (T)UInt160.deserialize(is);
		}
		return null;
	}
}
