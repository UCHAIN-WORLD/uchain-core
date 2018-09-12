package com.uchain.core.consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SortedMultiMap1<K, V> {
	private Map<K, List<V>> container = null;

	public SortedMultiMap1(String sortType) {
		if ("reverse".equals(sortType)) {
			this.container = new TreeMap<K, List<V>>(new MapKeyComparatorReverse<K>());
		} else {
			this.container = new TreeMap<K, List<V>>(new MapKeyComparator<K>());
		}
	}

	public int size() {
		int sizeBig = 0;
		for (Map.Entry<K, List<V>> entry : container.entrySet()) {
			sizeBig = sizeBig + entry.getValue().size();
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
		List<V> list = null;
		if (!container.containsKey(k)) {
			list = new ArrayList<V>();
			list.add(v);
			container.put(k, list);
		} else {
			container.get(k).add(v);
		}
	}

	public List<V> remove(K k) {
		return container.remove(k);
	}

	public TwoTuple<K, V> head() {
		return iterator().next();
	}

	public SortedMultiMap1Iterator<K, V> iterator() {
		return new SortedMultiMap1Iterator<K, V>(container);
	}
}
