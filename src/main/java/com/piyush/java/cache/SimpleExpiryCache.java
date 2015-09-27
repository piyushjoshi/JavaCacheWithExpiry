package com.piyush.java.cache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author jpiyush
 *
 *	Simple implementation of ExpiryCache.
 *
 *		Uses a scheduledExecutorService for cleaning up expired values, which gets expired key value pairs from
 *	a priority queue whose natural order is expiration timestamp. 
 *
 *	Values are wrapped as Expirable interface, which encapsulates logic of finding whether a key is expired or not.
 * @param <K>
 * @param <V>
 */
public class SimpleExpiryCache<K, V> implements ExpiryCache<K, V> {

	private ConcurrentMap<K, Expirable<V>> cache;

	private ScheduledExecutorService cleanupService;
	private BlockingQueue<ExpirableEntry<K, V>> expirationQueue;

	public SimpleExpiryCache() {
		cache = new ConcurrentHashMap<>();
		cleanupService = Executors.newScheduledThreadPool(1);
		expirationQueue = new PriorityBlockingQueue<>();
		startCleanupService();
	}

	/**
	 * Creates an expirable object wrapping the value provided.
	 * Inserts the expirable into cache map.
	 * Creates an expirableKeyValuePair object wrapping the expirable.
	 * Inserts the expirableKeyValuePair into the priority queue.
	 * 
	 * This method doesn't need any additional synchronization apart from that offered by ConcurrentMap and BlockingQueue implementations.
	 * 
	 * Time Complexity: O(log(n)), where n = total number of keys in cache
	 * 					it is the time required to add a value in priorityQueue, assuming it uses a binary heap.
	 */
	@Override
	public void put(K key, V value, int ttl, TimeUnit timeUnit) {
		Expirable<V> expirable = new ExpirableValue<>(value, ttl, timeUnit);
		cache.put(key, expirable);
		expirationQueue.offer(new ExpirableKeyValuePair<>(key, expirable));
	}

	/**
	 * Get method gets expirable object associated with the given key.
	 * Checks if it is expired or not.
	 * returns the wrapped value of expirable.
	 * 
	 * Time Complexity: O(1), non-blocking method.
	 */
	@Override
	public V get(K key) {
		Expirable<V> expirable = cache.get(key);
		if (expirable == null || expirable.isExpired()) {
			return null;
		} else {
			return expirable.getValue();
		}
	}

	/**
	 * This is the implementation of cleanup task which repeatedly cleans up expired values from Cache map.
	 * This doesn't iterate over map.
	 * It uses a PriorityBlockingQueue for getting most probably expired values.
	 * If the peek value of queue is not expired then all values of map are not expired, in that case it returns.
	 * Else it repeatedly takes expired values out of queue, and calls remove(key, value) method of cache map.
	 * 
	 * remove(key, value) method of ConcurrentHashMap ensures that the key is removed only if it is currently mapped to a given value.
	 * else it is not removed.
	 * This ensures that deletion of expiredKey does not have any effect on simultaneous put with same key.
	 */
	private void startCleanupService() {
		cleanupService.scheduleAtFixedRate(() -> {
			ExpirableEntry<K, V> expirableKeyValuePair = expirationQueue.peek();
			while (expirableKeyValuePair != null && expirableKeyValuePair.isExpired()) {
				expirableKeyValuePair = expirationQueue.poll();
				if (expirableKeyValuePair != null && expirableKeyValuePair.isExpired()) {
					cache.remove(expirableKeyValuePair.getKey(), expirableKeyValuePair.getExpirableValue());
				}
				expirableKeyValuePair = expirationQueue.peek();
			}
		}, 1, 2, TimeUnit.SECONDS);
	}
}
