package com.uchain.core.consensus;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SortedMultiMap1<K, V> {
	private Map<K, List<V>> container;

	public SortedMultiMap1(String sortType) {
	    this.container = new TreeMap(new MapKeyComparator<K>(sortType));
	}

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

	public void put(K k, V v) {
			if (!container.containsKey(k)) {
				container.put(k, Lists.newArrayList());
			}
				container.get(k).add(v);

	}

	public List<V> remove(K k) {
        return container.remove(k);

	}

	public TwoTuple<K, V> head() {
		return iterator().next();
	}

	public SortedMultiMap1Iterator<K, V> iterator() {
		return new SortedMultiMap1Iterator(container);
	}
}
