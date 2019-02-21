package com.uchain;

import com.uchain.core.consensus.SortedMultiMap1;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.UInt256;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class SortedMultiMap1Test {
    @Test
    public void testReverse() throws UnsupportedEncodingException {
//		SortedMultiMap1<String, String> sortedMultiMap1 = new SortedMultiMap1<String, String>("reverse");
//		sortedMultiMap1.put("2", "aa");
//		sortedMultiMap1.put("8", "bb");
//		sortedMultiMap1.put("2", "cc");
//		sortedMultiMap1.put("7", "dd");


        SortedMultiMap1<Integer, UInt256> sortedMultiMap2 = new SortedMultiMap1<Integer, UInt256>("asc");
        UInt256 ss0 = UInt256.fromBytes(Crypto.hash256(("test" + 0).getBytes("UTF-8")));
        UInt256 ss1 = UInt256.fromBytes(Crypto.hash256(("test" + 1).getBytes("UTF-8")));
        UInt256 ss2 = UInt256.fromBytes(Crypto.hash256(("test" + 2).getBytes("UTF-8")));
        UInt256 ss3 = UInt256.fromBytes(Crypto.hash256(("test" + 3).getBytes("UTF-8")));
        sortedMultiMap2.put(1, ss0);
        sortedMultiMap2.put(2, ss0);
        sortedMultiMap2.put(3, ss0);
        sortedMultiMap2.put(4, ss0);
        sortedMultiMap2.put(5, ss0);
        sortedMultiMap2.put(6, ss0);
        sortedMultiMap2.put(7, ss0);
        sortedMultiMap2.put(8, ss0);
        sortedMultiMap2.put(9, ss0);
        sortedMultiMap2.put(10, ss0);
        sortedMultiMap2.put(11, ss0);


        System.out.println(sortedMultiMap2.head().first);
        sortedMultiMap2.remove(1);
        System.out.println(sortedMultiMap2.size()+" "+sortedMultiMap2.head().first);


//        Map<Integer, List<String>> container = new TreeMap(new MapKeyComparator<Integer>());
//        List list=new ArrayList();list.add("string1");list.add("string2");
//        container.put(1000, null);
//        container.put(2,null);
//        container.put(3,list);
//        container.put(4,list);
//
//        System.out.println(container.size());
//        container.remove(2);
//        System.out.println(container.size());

    }
}
