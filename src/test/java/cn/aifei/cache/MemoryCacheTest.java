package cn.aifei.cache;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class MemoryCacheTest {

    @Test
    public void basicOps() {
        Cache cache = new CachePlugin(new CacheConfig().setType("memory")).createCache(new CacheConfig().setType("memory"));
        cache.put("k", "v");
        assertEquals("v", cache.get("k"));
        assertTrue(cache.exists("k"));
        cache.remove("k");
        assertNull(cache.get("k"));
    }

    @Test
    public void ttlWorks() throws Exception {
        Cache cache = new CachePlugin(new CacheConfig().setType("memory")).createCache(new CacheConfig().setType("memory"));
        cache.put("k", "v", 1);
        assertEquals("v", cache.get("k"));
        Thread.sleep(1100);
        assertNull(cache.get("k"));
    }

    @Test
    public void getOrSetWorks() {
        Cache cache = new CachePlugin(new CacheConfig().setType("memory")).createCache(new CacheConfig().setType("memory"));
        AtomicInteger n = new AtomicInteger();
        assertEquals("v", cache.getOrSet("k", () -> {
            n.incrementAndGet();
            return "v";
        }));
        assertEquals("v", cache.getOrSet("k", () -> {
            n.incrementAndGet();
            return "x";
        }));
        assertEquals(1, n.get());
    }
}
