package cn.aifei.cache.backend;

import java.io.Serializable;

public class CacheEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Object value;
    private final long expireAtMillis;

    public CacheEntry(Object value, long ttlSeconds) {
        this.value = value;
        this.expireAtMillis = ttlSeconds > 0 ? System.currentTimeMillis() + ttlSeconds * 1000 : 0;
    }

    public Object getValue() {
        return value;
    }

    public boolean isExpired() {
        return expireAtMillis > 0 && System.currentTimeMillis() >= expireAtMillis;
    }
}
