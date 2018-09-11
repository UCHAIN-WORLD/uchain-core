package com.uchain.core.consensus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class MultiMapIterator<K,V> implements Iterator<Object>{
	
	private Iterator<Map.Entry<K, List<V>>> it;

	private List<V> it2;
	private K k;

	public MultiMapIterator(Map<K, List<V>> container) {
		it = container.entrySet().iterator();
	}
	@Override
	public boolean hasNext() {
		if(it2 == null) {
			nextIt();
		}
		if(it2.size() >0) {
			return true;
		}else {
			return false;
		}
	}

	@Override
	public Map<K, List<V>> next() {
		if(!hasNext())
			throw new NoSuchElementException();
		Map<K, List<V>> map = new HashMap<K, List<V>>();
		map.put(k, it2);
		return map;
	}

	private void nextIt() {
		if(it.hasNext()) {
			Map.Entry<K, List<V>> next = it.next();
			it2 = next.getValue();
			k = next.getKey();
		}
	}
}
