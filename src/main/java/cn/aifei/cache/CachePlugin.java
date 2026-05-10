package cn.aifei.cache;

import cn.aifei.cache.backend.CaffeineCache;
import cn.aifei.cache.backend.EhcacheCache;
import cn.aifei.cache.backend.MemoryCache;
import cn.aifei.cache.backend.RedisCache;
import cn.aifei.plugin.Plugin;

public class CachePlugin implements Plugin {

    private final CacheConfig config;
    private Cache cache;

    public CachePlugin() {
        this(CacheConfig.fromPropKit());
    }

    public CachePlugin(CacheConfig config) {
        this.config = config;
    }

    @Override
    public void start() {
        cache = createCache(config);
        CacheKit.init(cache);
    }

    @Override
    public void stop() {
        if (cache != null) {
            cache.close();
            cache = null;
        }
        CacheKit.clearInit();
    }

    protected Cache createCache(CacheConfig config) {
        String type = config.getType() == null ? "memory" : config.getType().trim().toLowerCase();
        if ("memory".equals(type) || "map".equals(type)) {
            return new MemoryCache(config);
        }
        if ("caffeine".equals(type) || "caffine".equals(type)) {
            return new CaffeineCache(config);
        }
        if ("ehcache".equals(type) || "eh".equals(type)) {
            return new EhcacheCache(config);
        }
        if ("redis".equals(type)) {
            return new RedisCache(config);
        }
        throw new IllegalArgumentException("Unsupported cache.type: " + config.getType());
    }
}
