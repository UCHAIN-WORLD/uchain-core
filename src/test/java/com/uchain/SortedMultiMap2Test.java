package com.uchain;

import com.uchain.core.consensus.SortedMultiMap2;
import com.uchain.core.consensus.SortedMultiMap2Iterator;
import com.uchain.core.consensus.ThreeTuple;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.UInt256;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class SortedMultiMap2Test {
	@Test
	public void testReverse() throws UnsupportedEncodingException {
		SortedMultiMap2<Integer, Integer, UInt256> sortedMultiMap2 = new SortedMultiMap2<Integer, Integer, UInt256>(
				"reverse", "reverse");
		UInt256 ss = UInt256.fromBytes(Crypto.hash256(("test").getBytes("UTF-8")));
		sortedMultiMap2.put(2, 2, ss);
		sortedMultiMap2.put(8, 8, ss);
		sortedMultiMap2.put(2, 2, ss);
		sortedMultiMap2.put(7, 7, ss);

		SortedMultiMap2Iterator<Integer, Integer, UInt256> sortedMultiMap2Iterator = sortedMultiMap2.iterator();
		ThreeTuple<Integer, Integer, UInt256> three = sortedMultiMap2Iterator.next();
		assert (three.first.intValue()==8 && three.second.intValue() == 8);


		SortedMultiMap2<String, String, UInt256> sortedMultiMap21 = new SortedMultiMap2<String, String, UInt256>(
				"reverse", "reverse");
		UInt256 ss1 = UInt256.fromBytes(Crypto.hash256(("test").getBytes("UTF-8")));
		sortedMultiMap21.put("2", "2", ss1);
		sortedMultiMap21.put("8", "8", ss1);
		sortedMultiMap21.put("2", "2", ss1);
		sortedMultiMap21.put("7", "7", ss1);

		SortedMultiMap2Iterator<String, String, UInt256> sortedMultiMap2Iterator1 = sortedMultiMap21.iterator();
		ThreeTuple<String, String, UInt256> three1 = sortedMultiMap2Iterator1.next();
		assert (three1.first.equals("8") && three1.second.equals("8"));
		
		
		SortedMultiMap2<String, Boolean, UInt256> sortedMultiMap22 = new SortedMultiMap2<String, Boolean, UInt256>(
				"reverse", "reverse");
		UInt256 ss2 = UInt256.fromBytes(Crypto.hash256(("test").getBytes("UTF-8")));
		sortedMultiMap22.put("2", false, ss2);
		sortedMultiMap22.put("8", true, ss2);
		sortedMultiMap22.put("8", false, ss2);
		sortedMultiMap22.put("7", false, ss2);

		SortedMultiMap2Iterator<String, Boolean, UInt256> sortedMultiMap2Iterator2 = sortedMultiMap22.iterator();
		ThreeTuple<String, Boolean, UInt256> three2 = sortedMultiMap2Iterator2.next();
		assert (three2.first.equals("8") && three2.second.booleanValue() == true);


		SortedMultiMap2<Integer, Boolean, UInt256> sortedMultiMap23 = new SortedMultiMap2<>(
				"asc", "reverse");
		sortedMultiMap23.put(2, true, ss);
		sortedMultiMap23.put(8, true, ss);
		sortedMultiMap23.put(2, true, ss);
		sortedMultiMap23.put(7, true, ss);
		ThreeTuple<Integer, Boolean, UInt256> threeTuple = sortedMultiMap23.head();
		System.out.println(threeTuple.first);
	}
}
