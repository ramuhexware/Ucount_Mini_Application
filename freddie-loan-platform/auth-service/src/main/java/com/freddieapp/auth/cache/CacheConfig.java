package com.freddieapp.auth.cache;

import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class CacheConfig {

    private final Map<String, Object> tokenCache = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        tokenCache.put(key, value);
    }

    public Object get(String key) {
        return tokenCache.get(key);
    }

    public void evict(String key) {
        tokenCache.remove(key);
    }
}
