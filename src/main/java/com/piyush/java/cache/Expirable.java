package com.piyush.java.cache;

/**
 * 
 * @author jpiyush
 *
 *	Generic interface for representing expirable values.
 *
 * @param <V>
 */
public interface Expirable<V> {
	V getValue();

	boolean isExpired();

	long expireAt();
}
