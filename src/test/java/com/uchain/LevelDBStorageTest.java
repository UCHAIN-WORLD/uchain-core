package com.uchain;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.uchain.storage.ConnFacory;

import lombok.val;

public class LevelDBStorageTest {
	@Test
	public void testSet() {
		val storage = ConnFacory.getInstance("test_db");
		assert (true == storage.set("testSet".getBytes(), "testSetValue".getBytes()));
		storage.close();
	}

	@Test
	public void testGet() {
		val key = "testGet".getBytes();
		val valueString = "testGetValue".getBytes();
		val storage = ConnFacory.getInstance("test_db");
		assert (true == storage.set(key, valueString));
		val value = storage.get(key);
		assert (value != null);
		assert (new String(value).equals(new String(valueString)));
		storage.close();
	}

	@Test
	public void testUpdate() {
		val key = "testUpdate".getBytes();
		val valueString = "testUpdateValue";
		val newValueString = "testUpdateValueNew";
		val storage = ConnFacory.getInstance("test_db");
		assert (true == storage.set(key, valueString.getBytes()));
		val value = storage.get(key);
		assert (value != null);
		assert (new String(value).equals(valueString));
		assert (true == storage.set(key, newValueString.getBytes()));
		val newValue = storage.get(key);
		assert (newValue != null);
		assert (new String(newValue).equals(newValueString));
		storage.close();
	}

	@Test
	public void testGetKeyNotExists() {
		val storage = ConnFacory.getInstance("test_db");
		val value = storage.get("testNotExistKey".getBytes());
		assert (value == null);
		storage.close();
	}

	@Test
	public void testDelete() {
		val key = "testDelete".getBytes();
		val value = "testDeleteValue".getBytes();
		val storage = ConnFacory.getInstance("test_db");
		assert (true == storage.set(key, value));
		assert (storage.get(key) != null);
		storage.delete(key);
		assert (storage.get(key) == null);
		storage.close();
	}
	
	@Test
	public void  testScan() {
	    val storage = ConnFacory.getInstance("scan_test_db_1");
		Map<String, String> linkedHashMap = Maps.newLinkedHashMap();
	    for (int i = 1; i <= 10; i++) {
	    	val key = "key"+i;
	    	val value = "value"+i;
	    	linkedHashMap.put(key, value);
	    	if (storage.get(key.getBytes()) == null) {
		        assert(storage.set(key.getBytes(), value.getBytes()));
		    }
		}

	    int i = 0;
//	    Map<String, String> resultLinkedHashMap = storage.scan();
//	    for (Map.Entry<String, String> entry : resultLinkedHashMap.entrySet()) {
//	    	linkedHashMap.get(entry.getKey()).equals(entry.getValue());
//            i++;
//        }
	    assert(i == 10);
	    storage.close();
	  }
}
