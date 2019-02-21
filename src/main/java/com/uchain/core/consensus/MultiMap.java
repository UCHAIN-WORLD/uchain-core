package com.uchain.core.consensus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class MultiMap<K,V> implements Iterable<Object>{
	private Map<K, List<V>> container = Maps.newHashMap();
	
	public int size() {
		int sizeBig = 0;
		for (Map.Entry<K, List<V>> entry : container.entrySet()) {
			sizeBig += entry.getValue().size();
		}
		return sizeBig;
	}
	
	public boolean contains(K k) {
		return container.containsKey(k);
	}

	public List<V> get(K k) {
		return container.get(k);
	}

	public void put(K k,V v) {
		if (!container.containsKey(k)) {
			List<V> list = Lists.newArrayList();
			list.add(v);
			container.put(k, list);
		} else {
			container.get(k).add(v);
		}
	}
	
	public List<V> remove(K k) {
		return container.remove(k);
	}

	public Map<K, V> head() {
		return iterator().next();
	}
    public MultiMapIterator<K,V> iterator() {
    	return new MultiMapIterator(container);
    }
}
