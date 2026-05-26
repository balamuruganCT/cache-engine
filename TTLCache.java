package pt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by bala-zpt0052 on 09/04/26
 *
 */
public class TTLCache
{
	static class CacheData
	{
		private String value;
		private long expiryTimeInMs;

		public CacheData(String value, long ttlInMs)
		{
			this.value = value;
			this.expiryTimeInMs = System.currentTimeMillis() + ttlInMs;
		}

		public boolean isExpired()
		{
			return System.currentTimeMillis() > expiryTimeInMs;
		}
	}

	private Map<String, CacheData> cache;
	ReadWriteLock lock;

	public TTLCache()
	{
		this.cache = new ConcurrentHashMap<>();
		this.lock = new ReentrantReadWriteLock();
	}

	public void put(String key, String value, long ttlMS)
	{
		if (ttlMS < 0) {
			throw new IllegalArgumentException("ttlMS must be >= 0");
		}
		lock.writeLock().lock();
		try
		{
			// Overwrite existing entry and reset TTL
			cache.put(key, new CacheData(value, ttlMS));
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	public String get(String key)
	{
		boolean readLocked = false;
		lock.readLock().lock();
		readLocked = true;
		try
		{
			CacheData cacheData = cache.get(key);
			if (cacheData == null)
			{
				return null;
			}

			if (cacheData.isExpired())
			{
				// upgrade to write lock to remove expired entry
				lock.readLock().unlock();
				readLocked = false;
				lock.writeLock().lock();
				try
				{
					cacheData = cache.get(key);
					if (cacheData != null && cacheData.isExpired())
					{
						cache.remove(key);
					}
					return null;
				}
				finally
				{
					lock.writeLock().unlock();
				}
			}

			return cacheData.value;
		}
		finally
		{
			if (readLocked)
			{
				lock.readLock().unlock();
			}
		}
	}

	public void delete(String key)
	{
		lock.writeLock().lock();
		try
		{
			cache.remove(key);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	public void cleanUpPeriodic()
	{
		// Iterate over entries and remove expired ones. This can be called periodically by a scheduler.
		lock.writeLock().lock();
		try
		{
			for (Map.Entry<String, CacheData> entry : cache.entrySet())
			{
				CacheData cd = entry.getValue();
				if (cd != null && cd.isExpired())
				{
					cache.remove(entry.getKey(), cd);
				}
			}
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	public void print()
	{
		System.out.println(cache);
	}



	public static void main(String[] args)
	{
		TTLCache cache1 = new TTLCache();
		cache1.put("Bala", "Hiu", 11);
		cache1.print();
	}

}
