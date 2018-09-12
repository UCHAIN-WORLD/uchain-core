package com.uchain;

import java.util.Map;

import com.uchain.storage.LevelDbStorage;
import org.junit.*;

import com.google.common.collect.Maps;
import com.uchain.storage.ConnFacory;

import lombok.val;

public class LevelDBStorageTest {

	private static LevelDbStorage storage;

	@BeforeClass
	public static void setUp(){
		storage = ConnFacory.getInstance("\\.\\test_db");
	}

	@AfterClass
	public static void tearDown(){
		storage.close();
	}

	@Test
	public void testSet(){
		try {
			assert (storage.set("testSet".getBytes(), "testSetValue".getBytes()));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void testGet(){
		try {
			val key = "testGet".getBytes();
			val valueString = "testGetValue".getBytes();
			assert (storage.set(key, valueString));
			val value = storage.get(key);
			assert (value != null);
			assert (new String(value).equals(new String(valueString)));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdate(){
		try {
			val key = "testUpdate".getBytes();
			val valueString = "testUpdateValue";
			val newValueString = "testUpdateValueNew";
			assert (storage.set(key, valueString.getBytes()));
			val value = storage.get(key);
			assert (value != null);
			assert (new String(value).equals(valueString));
			assert (storage.set(key, newValueString.getBytes()));
			val newValue = storage.get(key);
			assert (newValue != null);
			assert (new String(newValue).equals(newValueString));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void testGetKeyNotExists(){
		val value = storage.get("testNotExistKey".getBytes());
		assert (value == null);
	}

	@Test
	public void testDelete(){
		val key = "testDelete".getBytes();
		val value = "testDeleteValue".getBytes();
		assert (storage.set(key, value));
		assert (storage.get(key) != null);
		storage.delete(key);
		assert (storage.get(key) == null);
	}
	
	@Test
	public void  testScan(){
		Map<String, String> linkedHashMap = Maps.newLinkedHashMap();
	    for (int i = 1; i <= 10; i++) {
	    	val key = "key"+i;
	    	val value = "value"+i;
	    	linkedHashMap.put(key, value);
	    	if (storage.get(key.getBytes()) == null) {
		        assert(storage.set(key.getBytes(), value.getBytes()));
		    }
		}
	  }
}
