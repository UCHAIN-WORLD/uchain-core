package com.uchain.common;

public interface Cache<K, V> {
	int size();

	boolean contains(K key);

	V get(K key);

	void set(K key, V value);

	void delete(K key);
}
