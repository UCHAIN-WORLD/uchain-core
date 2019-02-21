package com.uchain;

import com.google.common.collect.Maps;
import lombok.val;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LevelDBStorageTest {
	/*
	private static LevelDbStorage storage;

	@BeforeClass
	public static void setUp(){
		storage = ConnFacory.getInstance("test_net/fork");
	}

	@AfterClass
	public static void tearDown(){
		storage.close();
	}

	@Test
	public void testSet(){
		try {
			assert (storage.set("testSet".getBytes(), "testSetValue".getBytes(), storage.batchWrite()));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void testGet(){
		try {
			val key = "testGet".getBytes();
			val valueString = "testGetValue".getBytes();
			assert (storage.set(key, valueString, storage.batchWrite()));
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
			assert (storage.set(key, valueString.getBytes(), storage.batchWrite()));
			val value = storage.get(key);
			assert (value != null);
			assert (new String(value).equals(valueString));
			assert (storage.set(key, newValueString.getBytes(), storage.batchWrite()));
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
		assert (storage.set(key, value, storage.batchWrite()));
		assert (storage.get(key) != null);
		storage.delete(key, storage.batchWrite());
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
		        assert(storage.set(key.getBytes(), value.getBytes(), storage.batchWrite()));
		    }
		}
	  }
	  */
	private final String testClass = "LevelDBStorageTest";

	@AfterClass
	public static void cleanUp() {
		DBManager.clearUp("LevelDBStorageTest");
	}

	@Test
	public void testSet() {
		val storage = DBManager.open(testClass, "testSet");
		assert(storage.set("testSet".getBytes(), "testSetValue".getBytes(),storage.batchWrite()));
	}

	@Test
	public void testGet() {
		val key = "testGet".getBytes();
		val valueString = "testGetValue";
		val storage = DBManager.open(testClass, "testGet");
		assert(storage.set(key, valueString.getBytes(),null));
		val value = storage.get(key);
		assert (value != null);
		assert (new String(value).equals(new String(valueString)));
	}

	@Test
	public void testUpdate() {
		val key = "testUpdate".getBytes();
		val valueString = "testUpdateValue";
		val newValueString = "testUpdateValueNew";
		val storage = DBManager.open(testClass, "testUpdate");
		assert(true == storage.set(key, valueString.getBytes()));
		val value = storage.get(key);
		assert (value != null);
		assert (new String(value).equals(valueString));
		assert (storage.set(key, newValueString.getBytes()));
		val newValue = storage.get(key);
		assert (newValue != null);
		assert (new String(newValue).equals(newValueString));
	}

	@Test
	public void testGetKeyNotExists() {
		val storage = DBManager.open(testClass, "testGetKeyNotExists");
		val value = storage.get("testNotExistKey".getBytes());
		assert(value == null);
	}

	@Test
	public void testDelete() {
		val key = "testDelete".getBytes();
		val value = "testDeleteValue".getBytes();
		val storage = DBManager.open(testClass, "testDelete");
		assert (true == storage.set(key, value));
		assert (storage.get(key) != null);
		storage.delete(key, null);
		assert (storage.get(key) == null);
	}

	@Test
	public void testScan() {
		val storage = DBManager.open(testClass, "testScan");
		Map<String, String> linkedHashMap = Maps.newLinkedHashMap();
		TreeMap<String,String> tree = new TreeMap<>();
		for (int i = 1; i <= 10; i++) {
			val key = "key"+i;
			val value = "value"+i;
			linkedHashMap.put(key, value);
			tree.put(key,value);
			if (storage.get(key.getBytes()) == null) {
				assert(storage.set(key.getBytes(), value.getBytes()));
			}
		}

		int i = 0;
		List<Map.Entry<byte[], byte[]>> list = storage.scan(/*(k, v) -> {
			//assert((new String(k), new String(v)).equals(tree.get(String.valueOf(i))));
			//i++;
		}*/);
		for(Map.Entry<String,String> entry:tree.entrySet()){
			assert(new String(list.get(i).getKey()).equals(entry.getKey()));
			assert(new String(list.get(i).getValue()).equals(entry.getValue()));
			i++;
		}
		assert(i == 10);
	}

	@Test
	public void testFind() {
		val storage = DBManager.open(testClass, "testFind");
		TreeMap<String,String> treeA = new TreeMap<String,String>();
		TreeMap<String,String> treeB = new TreeMap<String,String>();
		List<TreeMap<String,String>> seqArr = new ArrayList<>();
		seqArr.add(treeA);
		seqArr.add(treeB);
		List<String> prefixes = new ArrayList();
		prefixes.add("key_a_");
		prefixes.add("key_b_");

		for (int i=1;i<=10;i++) {
			val key = prefixes.get(i%2)+i;
			val keyBytes = key.getBytes();
			val value = "value"+i;

//			seqArr(i % 2) = seqArr(i % 2) :+ (key, value)
			seqArr.get(i % 2).put(key,value);
			if (storage.get(keyBytes) == null) {
				assert(storage.set(keyBytes, value.getBytes()));
			}
		}

		for (int j=0;j<=1;j++) {
			int i = 0;
			val seq = seqArr.get(j);
			List<Map.Entry<byte[], byte[]>> twoTuple = storage.find(prefixes.get(j).getBytes()/*, (k, v) => {
				assert((new String(k), new String(v)).equals(seq(i)))
				i += 1
			}*/);
			for(Map.Entry<String,String> entry:seq.entrySet()){
				assert(new String(twoTuple.get(i).getKey()).equals(entry.getKey()));
				assert(new String(twoTuple.get(i).getValue()).equals(entry.getValue()));
				i++;
			}
			assert(i == 5);
		}
	}

	@Test
	public void testLastEmpty() throws IOException {
		val storage = DBManager.open(testClass, "testLastEmpty");
		assert(storage.last() == null);
	}

	@Test
	public void testLast() throws IOException {
		val storage = DBManager.open(testClass, "testLast");
		for (int i =0;i<= 10;i++) {
			storage.set(new BigInteger(String.valueOf(i)).toByteArray(), ("test"+i).getBytes());
		}

		val last = storage.last();
		assert(new BigInteger(last.getKey()).intValue() == 10);
		assert(new String(last.getValue()).equals("test10"));
	}

	public void assertUncommittedSessions(List<Integer> levels, Integer min, Integer max){
		assert(levels.size() == max - min + 1);
		Integer start = min;
		for (Integer elem :levels) {
			assert(elem == start);
			start += 1;
		}
		assert(start == max + 1);
	}

	@Test(expected = AssertionError.class)
	public void testSession() {
	try {
		val testMethod = "testSession";

		{
			val db = DBManager.open(testClass, testMethod);
			try {
				db.newSession();
				assert (db.revision() == 2);
				assertUncommittedSessions(db.uncommitted(), 1, 1);
			} finally {
				DBManager.close(testClass, testMethod);
			}
		}
		{
			val db = DBManager.open(testClass, testMethod);
			try {
				db.newSession();
				assert (db.revision() == 3);
				assertUncommittedSessions(db.uncommitted(), 1, 2);
			} finally {
				DBManager.close(testClass, testMethod);
			}
		}
		{
			val db = DBManager.open(testClass, testMethod);
			try {
				assert (db.revision() == 3);
				db.rollBack();
				assert (db.revision() == 2);
				assertUncommittedSessions(db.uncommitted(), 1, 1);
			} finally {
				DBManager.close(testClass, testMethod);
			}
		}
		{
			val db = DBManager.open(testClass, testMethod);
			try {
				assert (db.revision() == 2);
				db.commit();
				assert (db.uncommitted().size() == 0);
			} finally {
				DBManager.close(testClass, testMethod);
			}
		}
		{
			val db = DBManager.open(testClass, testMethod);
			try {
				assert (db.revision() == 2);
				assert (db.uncommitted().size() == 0);
				db.newSession();
				db.newSession();
				db.newSession();
				db.newSession();
				db.newSession();
				db.newSession();
				assert (db.revision() == 8);
			} finally {
				DBManager.close(testClass, testMethod);
			}
		}
		{
			val db = DBManager.open(testClass, testMethod);
			try {
				assert (db.revision() == 8);
				assertUncommittedSessions(db.uncommitted(), 2, 7);
				db.commit(5);
				assertUncommittedSessions(db.uncommitted(), 6, 7);
			} finally {
				DBManager.close(testClass, testMethod);
			}
		}
		{
			val db = DBManager.open(testClass, testMethod);
			try {
				assert (db.revision() == 8);
				assertUncommittedSessions(db.uncommitted(), 6, 7);
				db.rollBack();
				db.rollBack();
				assert (db.uncommitted().size() == 0);
				assert (db.revision() == 6);
			} finally {
				DBManager.close(testClass, testMethod);
			}
		}
		{
			val db = DBManager.open(testClass, testMethod);
			try {
				assert (db.uncommitted().size() == 0);
				assert (db.revision() == 6);
				System.out.println("final revision " + db.revision());
			} finally {
				DBManager.close(testClass, testMethod);
			}
		}
	}catch (Exception e){
		e.printStackTrace();
		return;
	}
	}
}
