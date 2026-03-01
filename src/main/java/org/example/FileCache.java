package org.example;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileCache {
    private final Map<String, byte[]> cache;

    public FileCache(int maxSize) {
        this.cache = Collections.synchronizedMap(new LinkedHashMap<String, byte[]>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                return size() > maxSize;
            }
        });
    }

    public byte[] get(String key) { return cache.get(key); }
    public void put(String key, byte[] content) { cache.put(key, content); }
    public void clear() { cache.clear(); }
    public int size() { return cache.size(); }
}

