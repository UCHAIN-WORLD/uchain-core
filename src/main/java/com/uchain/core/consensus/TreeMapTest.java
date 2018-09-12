package com.uchain.core.consensus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TreeMapTest {
	
	public static void main(String[] args) {
		SortedMultiMap1 sortedMultiMap1 = new SortedMultiMap1("reverse");
		sortedMultiMap1.put("2", "aa");
		sortedMultiMap1.put("2", "bb");
		sortedMultiMap1.put("8", "cc");
		sortedMultiMap1.put("7", "dd");
		
		SortedMultiMap1Iterator sortedMultiMap1Iterator = sortedMultiMap1.iterator();
		
		while(sortedMultiMap1Iterator.hasNext()) {
			sortedMultiMap1Iterator.next();
//			Set a = sortedMultiMap1Iterator.next().keySet();
//			for (Integer key : sortedMultiMap1Iterator.next().keySet()) { 
//				  System.out.println("Key = " + key); 
//				} 
		}
//		sortedMultiMap1.head();
//		sortedMultiMap1.nextIt();
//		sortedMultiMap1.head();
//		sortedMultiMap1.head();
//		System.out.println(sortedMultiMap1);
		
		
//		Map map = new HashMap();       //定义Map对象
//		  map.put(1, "新鲜的苹果");      //向集合中添加对象
//		  map.put(2, "配置优良的计算机");
//		  map.put(3, "堆积成山的图书");
//		  map.put(4, new Date()); 
//		  String key = "book"; 
//		  boolean contains = map.containsKey(1);    //判断是否包含指定的键值
//		  if (contains) {         //如果条件为真
//		   System.out.println("在Map集合中包含键名" + key); //输出信息
//		  } else {
//		   System.out.println("在Map集合中不包含键名" + key);
//		  }
		
//		List<Integer> nums = new ArrayList<Integer>();
//		nums.add(3);
//		nums.add(5);
//		nums.add(1);
//		nums.add(0);
//		System.out.println(nums);
//		Collections.sort(nums);
//		Collections.sort(nums,new Comparator<Integer>() {
//			public int compare(Integer o1, Integer o2) {  
//                //按照金额大小进行降序排列  
//                if(o1.intValue() < o2.intValue()){  
//                    return 1;  
//                }  
//                if(o1.intValue() == o2.intValue()){  
//                    return 0;  
//                }  
//                return -1;  
//            }  
//		});
//		System.out.println(nums);

	}
}