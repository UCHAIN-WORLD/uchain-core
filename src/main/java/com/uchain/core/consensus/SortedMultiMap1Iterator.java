package com.uchain.core.consensus;

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
	}
	@Override
	public boolean hasNext() {
		if(it2 == null || !it2.hasNext()) {
			nextIt();
		}
		return it2 != null && it2.hasNext();
	}

	@Override
	public TwoTuple<K, V> next() {
		if(!hasNext())
			throw new NoSuchElementException();
		TwoTuple<K, V> twoTuple = new TwoTuple(k, it2.next());
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
