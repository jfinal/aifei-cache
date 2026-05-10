package cn.aifei.cache.backend;

import cn.aifei.cache.CacheConfig;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MemoryCache extends AbstractCache {

    private final ConcurrentMap<String, ConcurrentMap<String, CacheEntry>> store = new ConcurrentHashMap<>();

    public MemoryCache(CacheConfig config) {
        super(config);
    }

    @Override
    protected Object getValue(String cacheName, String key) {
        ConcurrentMap<String, CacheEntry> cache = store.get(cacheName);
        if (cache == null) {
            return null;
        }
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        return entry.getValue();
    }

    @Override
    protected void putValue(String cacheName, String key, Object value, long ttlSeconds) {
        store.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>())
                .put(key, new CacheEntry(value, ttlSeconds));
    }

    @Override
    public boolean exists(String cacheName, String key) {
        return getValue(cacheName, key) != null;
    }

    @Override
    public void remove(String cacheName, String key) {
        ConcurrentMap<String, CacheEntry> cache = store.get(cacheName);
        if (cache != null) {
            cache.remove(key);
        }
    }

    @Override
    public void clear(String cacheName) {
        ConcurrentMap<String, CacheEntry> cache = store.get(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    @Override
    public void clearAll() {
        store.clear();
    }

    @Override
    public long incr(String cacheName, String key, long delta) {
        ConcurrentMap<String, CacheEntry> cache = store.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>());
        synchronized (cache) {
            Object old = getValue(cacheName, key);
            long next = (old instanceof Number ? ((Number) old).longValue() : 0L) + delta;
            cache.put(key, new CacheEntry(next, config.getDefaultTtlSeconds()));
            return next;
        }
    }

    @Override
    public void close() {
        clearAll();
    }
}
