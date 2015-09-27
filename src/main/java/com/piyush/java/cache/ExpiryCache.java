package com.piyush.java.cache;

import java.util.concurrent.TimeUnit;

public interface ExpiryCache<K, V> {
	
	/** put this entry(key,value) in the Cache with provided ttl.
	* If cache.get happens within this ttl, value should be returned else cache.get should return
	null
	* @param key
	* @param value
	* @param ttl ­ how long this entry should remain in Cache, in units of timUnit
	* @param timeUnit ­ a TimeUnit (ref: java.util.concurrent.TimeUint) determining how to interpret the timeout
	parameter
	*/
	void put(K key, V value, int ttl, TimeUnit timeUnit);

	/**
	* get entry.value from cache for this key unless that is expired.
	* @param key
	* @return value associated with the key
	*/
	V get(K key);
}
