package com.piyush.java.cache;

/**
 * 
 * @author jpiyush
 * 
 *	Generic interface for representing expirable key value pairs.
 *
 * @param <K>
 * @param <V>
 */

public interface ExpirableEntry<K, V> extends Comparable<ExpirableEntry<K, V>>{
	K getKey();

	Expirable<V> getExpirableValue();

	boolean isExpired();
}
