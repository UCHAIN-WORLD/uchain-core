package com.uchain.common;

public class LRUCache<K, V> implements Cache<K, V> {
	private int capacity;

	public LRUCache(int capacity) {
		this.capacity = capacity;
		container = new LRUMap<K, V>(capacity);
	}

	@SuppressWarnings("serial")
	static class LRUMap<K, V> extends java.util.LinkedHashMap<K, V> {
		private int MAX_ENTRIES = 0;

		public LRUMap(int capacity) {
			super(capacity, 1.0f, true);
			MAX_ENTRIES = capacity;
		}

		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
			return size() > MAX_ENTRIES;
		}
	}

	LRUMap<K, V> container ;

	@Override
	public int size() {
		return container.size();
	}

	@Override
	public boolean contains(K key) {
		return container.containsKey(key);
	}

	@Override
	public V get(K key) {
		V value = container.get(key);
		if (value == null) {
			return null;
		} else {
			return value;
		}
	}

	@Override
	public void set(K key, V value) {
		container.put(key, value);
	}

	@Override
	public void delete(K key) {
		container.remove(key);
	}
}
