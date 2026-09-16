package com.freddieapp.underwriting.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UnderwritingCache {
    private final Map<String, Object> cacheStore = new ConcurrentHashMap<>();

    public void put(String key, Object value) { cacheStore.put(key, value); }
    public Object get(String key) { return cacheStore.get(key); }
    public boolean containsKey(String key) { return cacheStore.containsKey(key); }
}
