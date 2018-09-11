package com.uchain.core.consensus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiMap<K,V>{
	private Map<K, List<V>> container = new HashMap<K, List<V>>();
	
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

	public void put(K k,V v) {
		if(!container.containsKey(k)) {
			container.put(k, new ArrayList<V>());
		}
		container.get(k).add(v);
	}
	
	public List<V> remove(K k) {
		return container.remove(k);
	}

	public Map<K, List<V>> head() {
		return iterator().next();
	}
    public MultiMapIterator<K,V> iterator() {
    	return new MultiMapIterator<K, V>(container);
    }
}
