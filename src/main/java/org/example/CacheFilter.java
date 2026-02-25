package org.example;

import java.io.IOException;
import java.util.Objects;

public class CacheFilter {
    private final FileCache cache = new FileCache();

    public byte[] getOrFetch(String uri, FileProvider provider) throws IOException {
        Objects.requireNonNull(uri, "URI kan inte vara null");
        Objects.requireNonNull(provider, "Provider kan inte vara null");

        if (cache.contains(uri)) {
            logCacheHit(uri);
            return cache.get(uri);
        }

        logCacheMiss(uri);
        byte[] fileBytes = provider.fetch(uri);

        if (fileBytes != null) {
            cache.put(uri, fileBytes);
        }
        return fileBytes;
    }

    private void logCacheHit(String uri) {
        System.out.println("Cache hit for: " + uri);
    }

    private void logCacheMiss(String uri) {
        System.out.println("Cache miss for: " + uri);
    }

    @FunctionalInterface
    public interface FileProvider {
        byte[] fetch(String uri) throws IOException;
    }
}
