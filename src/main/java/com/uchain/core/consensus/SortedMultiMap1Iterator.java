package com.uchain.core.consensus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class SortedMultiMap1Iterator<K,V> implements Iterator<Object>{
	
	private Iterator<Map.Entry<K, List<V>>> it;

	private Iterator<V> it2;
	private K k;

	public SortedMultiMap1Iterator(Map<K, List<V>> container) {
		it = container.entrySet().iterator();
//		while(it.hasNext()) {
//			Map.Entry<K, List<V>> next = it.next();
//			System.out.println(next.getKey());
//		}
	}
	@Override
	public boolean hasNext() {
		if(it2 == null) {
			nextIt();
			return true;
		}
		if(it2.hasNext()) {
			return true;
		}
//		else if(it2 != null && !it2.hasNext()){
//			nextIt();
//			return true;
//		}
		return false;
	}

	@Override
	public TwoTuple<K, V> next() {
		if(!hasNext())
			throw new NoSuchElementException();
//		Map<K, V> map = new HashMap<K, V>();
//		V a = it2.next();
//		System.out.println("a="+a);
//		map.put(k, a);
//		return map;
		TwoTuple<K, V> twoTuple = new TwoTuple<K, V>(k, it2.next());
		return twoTuple;
	}

	private void nextIt() {
		if(it.hasNext()) {
			Map.Entry<K, List<V>> next = it.next();
			it2 = next.getValue().iterator();
			k = next.getKey();
		}
	}
}
