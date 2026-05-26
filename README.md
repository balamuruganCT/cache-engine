# cache-engine

Lightweight cache implementations in Java: an LRU (least-recently-used) cache and a TTL (time-to-live) cache.

This repository is intended for learning and small demo usage. The code lives in package `pt` and contains two standalone classes:

- `LRUCache.java` — integer key/value LRU cache implemented with a HashMap + doubly-linked list.
- `TTLCache.java` — String key/value TTL cache implemented with a ConcurrentHashMap and ReadWriteLock.

## Files

- `LRUCache.java` — LRU cache implementation and helpers.
- `TTLCache.java` — TTL cache implementation with a small `main` demo and a `cleanUpPeriodic` method.

## Quick start (compile & run)

From the repository root run:

```bash
# compile
javac -d out $(find . -name "*.java")

# run the TTLCache demo (class is in package `pt`)
java -cp out pt.TTLCache
```

If you only want to compile a single file, point `javac` at it and set `-d out` so the package structure is preserved.

## Usage examples

LRUCache (API)

- Constructor: `new LRUCache(int capacity)`
- `int get(int key)` — returns the value or `-1` if not present. Marks the key as recently used.
- `void put(int key, int value)` — inserts or updates the key. Evicts the least-recently-used entry when capacity is exceeded.

Example:

```java
LRUCache cache = new LRUCache(2);
cache.put(1, 1);
cache.put(2, 2);
System.out.println(cache.get(1)); // -> 1
cache.put(3, 3); // evicts key 2
System.out.println(cache.get(2)); // -> -1
```

TTLCache (API)

- Constructor: `new TTLCache()`
- `void put(String key, String value, long ttlMS)` — store an entry with TTL in milliseconds. This implementation overwrites existing keys and resets their TTL.
- `String get(String key)` — returns the value or `null` if missing or expired. Expired entries are removed on access.
- `void delete(String key)` — remove an entry.
- `void cleanUpPeriodic()` — iterate and remove expired entries (call from a scheduler for background cleanup).

Example:

```java
TTLCache t = new TTLCache();
t.put("foo", "bar", 5000); // ttl 5s
String v = t.get("foo"); // -> "bar" if within 5s, otherwise null
```

## Design notes & important behavior

- LRUCache uses a sentinel head/tail and a doubly-linked list to maintain recency order. The implementation was updated to be null-safe when removing nodes and to validate capacity > 0.
- TTLCache uses `ConcurrentHashMap` and a `ReentrantReadWriteLock` to coordinate reads and removals. The `get` method safely upgrades from read to write lock when it finds an expired entry.

## Thread-safety

- `LRUCache` is not thread-safe. If you need concurrent access, wrap calls in synchronization or add appropriate locking.
- `TTLCache` is designed with concurrency in mind (uses `ConcurrentHashMap` + read/write lock), but be careful when adding background cleanup threads — ensure proper lifecycle management.

## Known improvements you can make

- Add a scheduled background cleaner for `TTLCache` (use `ScheduledExecutorService`) and a shutdown method.
- Add unit tests (JUnit) covering eviction order, overwrite semantics, and TTL expiry under concurrency.
- Consider making `LRUCache` thread-safe if required (coarse-grained synchronized methods or fine-grained locks).

## Contributing

Feel free to open issues or PRs. For concurrency changes include tests demonstrating correctness.