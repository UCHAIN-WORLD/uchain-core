package com.uchain;

import org.junit.Test;

import com.uchain.core.consensus.SortedMultiMap1;
import com.uchain.core.consensus.SortedMultiMap1Iterator;

public class SortedMultiMap1Test {
	@Test
	public void testReverse() {
		SortedMultiMap1<String, String> sortedMultiMap1 = new SortedMultiMap1<String, String>("reverse");
		sortedMultiMap1.put("2", "aa");
		sortedMultiMap1.put("8", "bb");
		sortedMultiMap1.put("2", "cc");
		sortedMultiMap1.put("7", "dd");
		
		
		SortedMultiMap1<String, String> sortedMultiMap2 = new SortedMultiMap1<String, String>("asc");
		sortedMultiMap2.put("2", "aa");
		sortedMultiMap2.put("8", "bb");
		sortedMultiMap2.put("2", "cc");
		sortedMultiMap2.put("7", "dd");
		
		SortedMultiMap1Iterator<String, String> sortedMultiMap1Iterator = sortedMultiMap1.iterator();
		assert("8".equals(sortedMultiMap1Iterator.next().first));
		
		SortedMultiMap1Iterator<String, String> sortedMultiMap2Iterator = sortedMultiMap2.iterator();
		assert("2".equals(sortedMultiMap2Iterator.next().first));
//		
//		Map<Integer,List> map = Maps.newHashMap();
//		map.put(2, new ArrayList());
//		System.out.println(map.get(2));
//		SortedMultiMap1<Integer, String> sortedMultiMap3 = new SortedMultiMap1<Integer, String>("asc");
//		sortedMultiMap3.put(2, "aa");
//		sortedMultiMap3.put(8, "bb");
//		sortedMultiMap3.put(2, "cc");
//		sortedMultiMap3.put(7, "dd");
//		SortedMultiMap1Iterator<Integer, String> sorted3 = sortedMultiMap3.iterator();
//		while(sorted3.hasNext()) {
//			TwoTuple<Integer, String> two = sorted3.next();
//			System.out.println(two.first);
//		}
	}
}
