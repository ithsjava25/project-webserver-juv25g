package org.example;

import java.io.IOException;

public class CacheFilter {
    private final FileCache cache = new FileCache();
    
    public byte[] getOrFetch(String uri, FileProvider provider) throws IOException {
        if (cache.contains(uri)) {
            System.out.println("✓ Cache hit for: " + uri);
            return cache.get(uri);
        }
        System.out.println("✗ Cache miss for: " + uri);
        byte[] fileBytes = provider.fetch(uri);
        cache.put(uri, fileBytes);
        return fileBytes;
    }
    
    @FunctionalInterface
    public interface FileProvider {
        byte[] fetch(String uri) throws IOException;
    }
}
