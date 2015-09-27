package com.piyush.java.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author jpiyush
 *
 *	Simple implementation of Expirable interface, particularly suited for my implementation of ExpiryCache
 *
 * @param <V>
 */
public class ExpirableValue<V> implements Expirable<V> {

	private V value;
	private long creationTimestamp;
	private long expirationTimestamp;
	private boolean expired = false;

	public ExpirableValue(V value, long ttl, TimeUnit timeUnit) {
		this.value = value;
		this.creationTimestamp = System.currentTimeMillis();
		this.expirationTimestamp = getExpirationTimestamp(ttl, timeUnit);
	}

	public V getValue() {
		return value;
	}

	public boolean isExpired() {
		if (!expired) {
			expired = System.currentTimeMillis() >= expirationTimestamp;
		}
		return expired;
	}

	private long getExpirationTimestamp(long ttl, TimeUnit timeUnit) {
		return (long) (ttl * timeScaleFactor.get(timeUnit.name())) + creationTimestamp;
	}

	private static Map<String, Double> timeScaleFactor;
	static {
		timeScaleFactor = new HashMap<>();
		timeScaleFactor.put(TimeUnit.DAYS.name(), Double.valueOf(86400 * 1000));
		timeScaleFactor.put(TimeUnit.HOURS.name(), Double.valueOf(3600 * 1000));
		timeScaleFactor.put(TimeUnit.MINUTES.name(), Double.valueOf(60000));
		timeScaleFactor.put(TimeUnit.SECONDS.name(), Double.valueOf(1000));
		timeScaleFactor.put(TimeUnit.MILLISECONDS.name(), Double.valueOf(1));
		timeScaleFactor.put(TimeUnit.MICROSECONDS.name(), Double.valueOf(Math.pow(10, -3)));
		timeScaleFactor.put(TimeUnit.NANOSECONDS.name(), Double.valueOf(Math.pow(10, -6)));
	}

	@Override
	public long expireAt() {
		return expirationTimestamp;
	}

	@Override
	public String toString() {
		return String.format("Value:%s, creationTimestamp:%d, expirationTimestamp:%d, expired:%s", value.toString(), creationTimestamp, expirationTimestamp,
				String.valueOf(expired));
	}
}