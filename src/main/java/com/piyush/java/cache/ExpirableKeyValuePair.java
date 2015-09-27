package com.piyush.java.cache;

/**
 * 
 * @author jpiyush
 *
 *	This Object wraps any expirable value and associates a key to it.
 *	Also orders two ExpirableKeyValuePairs on the basis of expiration timestamp of their wrapped expirable values.
 *
 * @param <K>
 * @param <V>
 */
public class ExpirableKeyValuePair<K, V> implements ExpirableEntry<K, V> {

	private K key;
	private Expirable<V> expirableValue;

	public ExpirableKeyValuePair(K key, Expirable<V> expirableValue) {
		this.key = key;
		this.expirableValue = expirableValue;
	}

	@Override
	public K getKey() {
		return this.key;
	}

	@Override
	public Expirable<V> getExpirableValue() {
		return this.expirableValue;
	}

	@Override
	public boolean isExpired() {
		return expirableValue.isExpired();
	}

	@Override
	public int compareTo(ExpirableEntry<K, V> otherPair) {
		return (int) (this.expirableValue.expireAt() - otherPair.getExpirableValue().expireAt());
	}

}
