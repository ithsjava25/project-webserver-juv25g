
package org.example;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe file cache med LRU eviction policy.
 * Lagrar upp till MAX_CACHE_SIZE bytes totalt.
 */
public class FileCache {
    private static final long MAX_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_ENTRIES = 1000;

    private final Map<String, CacheEntry> cache;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private long currentSize = 0;

    public FileCache() {
        // LinkedHashMap för att kunna implementera LRU
        this.cache = new LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return cache.size() > MAX_ENTRIES || currentSize > MAX_CACHE_SIZE;
            }
        };
    }

    public boolean contains(String key) {
        Objects.requireNonNull(key, "Key kan inte vara null");
        lock.readLock().lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public byte[] get(String key) {
        Objects.requireNonNull(key, "Key kan inte vara null");
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(key);
            return entry != null ? entry.data : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void put(String key, byte[] value) {
        Objects.requireNonNull(key, "Key kan inte vara null");
        Objects.requireNonNull(value, "Value kan inte vara null");

        lock.writeLock().lock();
        try {
            // Ta bort gamla posten om den finns för att uppdatera storlek
            CacheEntry oldEntry = cache.remove(key);
            if (oldEntry != null) {
                currentSize -= oldEntry.data.length;
            }

            // Lägg till ny post
            cache.put(key, new CacheEntry(value));
            currentSize += value.length;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            currentSize = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public long getCurrentSizeInBytes() {
        lock.readLock().lock();
        try {
            return currentSize;
        } finally {
            lock.readLock().unlock();
        }
    }

    private static class CacheEntry {
        final byte[] data;
        final long timestamp;

        CacheEntry(byte[] data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
