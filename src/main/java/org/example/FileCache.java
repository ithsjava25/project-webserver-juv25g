
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
        // LinkedHashMap med access-order LRU
        // accessOrder=true betyder: get() och put() uppdaterar ordningen
        this.cache = new LinkedHashMap<String, CacheEntry>(16, 0.75f, true);
    }

    /**
     * Hämta data från cache OCH uppdatera LRU-ordningen.
     * MÅSTE ha write-lock eftersom get() modifierar LinkedHashMap's internal state.
     */
    public byte[] get(String key) {
        Objects.requireNonNull(key, "Key kan inte vara null");
        lock.writeLock().lock();  // ← WRITE-LOCK, inte read-lock!
        try {
            CacheEntry entry = cache.get(key);
            return entry != null ? entry.data : null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Hämta data utan att uppdatera LRU-ordningen (för diagnostik).
     * Kan använda read-lock eftersom vi inte modifierar LinkedHashMap's state.
     */
    public byte[] peek(String key) {
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
            // Guard mot filer större än cache
            if (value.length > MAX_CACHE_SIZE) {
                System.out.println("⚠️ Skipping oversized entry: " + key + 
                    " (" + (value.length / 1024 / 1024) + "MB > " + 
                    (MAX_CACHE_SIZE / 1024 / 1024) + "MB)");
                return;
            }

            // Ta bort gamla posten om den finns för att uppdatera storlek
            CacheEntry oldEntry = cache.remove(key);
            if (oldEntry != null) {
                currentSize -= oldEntry.data.length;
            }

            // Evicta medan nödvändigt INNAN vi lägger till ny post
            while ((currentSize + value.length > MAX_CACHE_SIZE || 
                    cache.size() >= MAX_ENTRIES) && !cache.isEmpty()) {
                evictLeastRecentlyUsedUnsafe();
            }

            // Lägg till ny post
            cache.put(key, new CacheEntry(value));
            currentSize += value.length;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Evicta minst nyligen använd entry (MÅSTE VARA UNDER WRITE LOCK)
     */
    private void evictLeastRecentlyUsedUnsafe() {
        // LinkedHashMap är sorterad efter access order (LRU)
        // Första entry är den minst nyligen använda
        Map.Entry<String, CacheEntry> eldest = cache.entrySet().iterator().next();
        
        String key = eldest.getKey();
        CacheEntry entry = eldest.getValue();
        
        cache.remove(key);
        currentSize -= entry.data.length;
        
        System.out.println("✗ Evicted from cache: " + key + 
            " (" + (entry.data.length / 1024) + "KB)");
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

    public boolean contains(String key) {
        lock.readLock().lock();
        try {
            return cache.containsKey(key);
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
