package cn.aifei.cache.backend;

import cn.aifei.cache.CacheConfig;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Iterator;
import java.util.Map;

public class CaffeineCache extends AbstractCache {

    private static final String SEP = "\u001f";

    private final com.github.benmanes.caffeine.cache.Cache<String, CacheEntry> store;

    public CaffeineCache(CacheConfig config) {
        super(config);
        this.store = Caffeine.newBuilder()
                .maximumSize(config.getMaxSize())
                .build();
    }

    @Override
    protected Object getValue(String cacheName, String key) {
        String realKey = realKey(cacheName, key);
        CacheEntry entry = store.getIfPresent(realKey);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            store.invalidate(realKey);
            return null;
        }
        return entry.getValue();
    }

    @Override
    protected void putValue(String cacheName, String key, Object value, long ttlSeconds) {
        store.put(realKey(cacheName, key), new CacheEntry(value, ttlSeconds));
    }

    @Override
    public boolean exists(String cacheName, String key) {
        return getValue(cacheName, key) != null;
    }

    @Override
    public void remove(String cacheName, String key) {
        store.invalidate(realKey(cacheName, key));
    }

    @Override
    public void clear(String cacheName) {
        String prefix = cacheName + SEP;
        Iterator<String> it = store.asMap().keySet().iterator();
        while (it.hasNext()) {
            if (it.next().startsWith(prefix)) {
                it.remove();
            }
        }
    }

    @Override
    public void clearAll() {
        store.invalidateAll();
    }

    @Override
    public long incr(String cacheName, String key, long delta) {
        String realKey = realKey(cacheName, key);
        Map<String, CacheEntry> map = store.asMap();
        synchronized (map) {
            Object old = getValue(cacheName, key);
            long next = (old instanceof Number ? ((Number) old).longValue() : 0L) + delta;
            store.put(realKey, new CacheEntry(next, config.getDefaultTtlSeconds()));
            return next;
        }
    }

    @Override
    public void close() {
        clearAll();
        store.cleanUp();
    }

    private String realKey(String cacheName, String key) {
        return cacheName + SEP + key;
    }
}
