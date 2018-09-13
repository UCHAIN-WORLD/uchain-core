package com.uchain.core.consensus;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class SortedMultiMap2Iterator<K1,K2,V> implements Iterator<Object>{
	
	private Iterator<Map.Entry<K1, SortedMultiMap1<K2,V>>> it;

	private SortedMultiMap1Iterator<K2, V> it2;
	private K1 k;

	public SortedMultiMap2Iterator(Map<K1, SortedMultiMap1<K2, V>> container) {
		it = container.entrySet().iterator();
	}
	@Override
	public boolean hasNext() {
		if(it2 == null) {
			nextIt();
		}
		if(it2.hasNext()) {
			return true;
		}
		return false;
	}
	
	@Override
	public ThreeTuple<K1,K2,V> next() {
		if(!hasNext())
			throw new NoSuchElementException();
//		Map<K2, V> next = it2.next();
//		System.out.println(next.size());
//		Map<K1,Map<K2, V>> map = new HashMap<K1,Map<K2, V>>();
//		map.put(k, next);
//		return map;
		TwoTuple<K2, V> twoTuple = it2.next();
		ThreeTuple<K1,K2,V> threeTuple = new ThreeTuple<K1,K2,V>(k, twoTuple.first, twoTuple.second);
		return threeTuple;
	}

	private void nextIt() {
		if(it.hasNext()) {
			Map.Entry<K1, SortedMultiMap1<K2,V>> next = it.next();
			it2 = next.getValue().iterator();
			k = next.getKey();
		}
	}
	
}
