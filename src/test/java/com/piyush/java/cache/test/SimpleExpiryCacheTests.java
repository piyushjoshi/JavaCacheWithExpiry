package com.piyush.java.cache.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.jodah.concurrentunit.Waiter;

import org.junit.Test;

import com.piyush.java.cache.ExpiryCache;
import com.piyush.java.cache.SimpleExpiryCache;

public class SimpleExpiryCacheTests {

	private ExecutorService executorService = Executors.newCachedThreadPool();

	@Test
	public void shouldExpireKeys() throws TimeoutException {
		final Waiter waiter = new Waiter();

		ExpiryCache<String, String> simpleExpiryCache = new SimpleExpiryCache<>();

		putValuesInCache(simpleExpiryCache, waiter);

		assertValuesInCache(simpleExpiryCache, waiter);

	}

	private void assertValuesInCache(ExpiryCache<String, String> simpleExpiryCache, Waiter waiter) throws TimeoutException {
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key1", "value1", 0));
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key2", "value2", 0));
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key3", "value3", 0));
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key4", "value4", 0));

		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key1", "value1", 5000));
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key2", null, 5000));
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key3", "value3", 5000));
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key4", null, 5000));

		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key1", null, 10000));
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key2", null, 10000));
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key3", null, 10000));
		executorService.execute(createAssertionTask(simpleExpiryCache, waiter, "key4", null, 10000));

		waiter.await(30000, 12);
	}

	private void putValuesInCache(final ExpiryCache<String, String> simpleExpiryCache, final Waiter waiter) throws TimeoutException {
		executorService.execute(createInsertionTask(simpleExpiryCache, "key1", "value1", 10000, waiter));
		executorService.execute(createInsertionTask(simpleExpiryCache, "key2", "value2", 5000, waiter));
		executorService.execute(createInsertionTask(simpleExpiryCache, "key3", "value3", 10000, waiter));
		executorService.execute(createInsertionTask(simpleExpiryCache, "key4", "value4", 5000, waiter));

		waiter.await(30000, 4);
	}

	private Runnable createInsertionTask(final ExpiryCache<String, String> simpleExpiryCache, final String key, final String value, int ttl, final Waiter waiter) {
		return new Runnable() {
			@Override
			public void run() {
				simpleExpiryCache.put(key, value, ttl, TimeUnit.MILLISECONDS);
				System.out.println(String.format("Putting values into cache. Key: %s, Value: %s, TTL: %dms", key, value, ttl));
				waiter.resume();
			}
		};
	}

	private Runnable createAssertionTask(final ExpiryCache<String, String> simpleExpiryCache, final Waiter waiter, final String key,
			final String expectedValue, final long delay) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					if (delay > 0)
						Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				String actualValue = simpleExpiryCache.get(key);
				waiter.assertEquals(expectedValue, actualValue);
				System.out.println(String.format("Assertion: getting value from cache for key: %s, Expected Value: %s, Actual Value: %s", key, expectedValue,
						actualValue));
				waiter.resume();
			}
		};
	}

}
