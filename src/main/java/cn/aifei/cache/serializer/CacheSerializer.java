package cn.aifei.cache.serializer;

public interface CacheSerializer {
    byte[] serialize(Object value);
    Object deserialize(byte[] bytes);
}
