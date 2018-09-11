package com.uchain.core.consensus;

import java.util.Comparator;

class MapKeyComparatorReverse<K> implements Comparator<K>{
	@Override
	public int compare(K k1, K k2) {
		if (k1 instanceof Integer) {
			int value1 = ((Integer) k1).intValue(); 
			int value2 = ((Integer) k2).intValue(); 
			if(value1>=value2) {
				return -1;
			}else {
				return 1;
			}
		}else if (k1 instanceof String) {
		    String s1 = (String) k1;
		    String s2 = (String) k2;
		    return s2.compareTo(s1);
		}else if (k1 instanceof Boolean) {
		    boolean b1 = ((Boolean) k1).booleanValue();
		    boolean b2 = ((Boolean) k2).booleanValue();
		    
		}
		return 0;
	}
}