package com.uchain.core.consensus;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SortedMultiMap2<K1,K2,V> implements Iterable<Object>{
	private Map<K1,SortedMultiMap1<K2,V>> container = new TreeMap<K1,SortedMultiMap1<K2,V>>();
	private String sortedMultiMap1SortType;
	public SortedMultiMap2(String sortType,String sortedMultiMap1SortType) {
		if ("reverse".equals(sortType)) {
			this.container = new TreeMap<K1,SortedMultiMap1<K2,V>>();
		} else {
			this.container = new TreeMap<K1,SortedMultiMap1<K2,V>>();
		}
		this.sortedMultiMap1SortType = sortedMultiMap1SortType;
	}
	
	public int size() {
		int sizeBig = 0;
		for (Map.Entry<K1, SortedMultiMap1<K2,V>> entry : container.entrySet()) {
			sizeBig = sizeBig + entry.getValue().size();
		}
		return sizeBig;
	}
	
	public boolean contains(K1 k1,K2 k2) {
		return container.containsKey(k1) && container.get(k1).contains(k2);
	}
	
	public List<V> get(K1 k1,K2 k2) {
		return container.get(k1).get(k2);
	}

	public void put(K1 k1,K2 k2,V v) {
		if(!container.containsKey(k1)) {
			container.put(k1, new SortedMultiMap1<K2,V>(sortedMultiMap1SortType));
		}
		container.get(k1).put(k2, v);
	}
	
	public List<V> remove(K1 k1,K2 k2) {
		if(container.containsKey(k1)) {
			List<V> list = container.get(k1).remove(k2);
			if(list.size() == 0) {
				container.remove(k1);
			}
			return list;
		}else {
			return null;
		}
	}

	public ThreeTuple<K1,K2,V> head() {
		return iterator().next();
	}
    public SortedMultiMap2Iterator<K1,K2,V> iterator() {
    	return new SortedMultiMap2Iterator<K1,K2,V>(container);
    }
}
