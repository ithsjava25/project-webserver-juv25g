package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FileCacheTest {
    private FileCache cache;

    @BeforeEach
    void setUp() {
        cache = new FileCache();
    }

    @Test
    void testPutAndGet() {
        byte[] fileContent = "Hello World".getBytes();
        cache.put("test.html", fileContent);

        byte[] retrieved = cache.get("test.html");
        assertThat(retrieved).isEqualTo(fileContent);
    }

    @Test
    void testContainsReturnsTrueAfterPut() {
        byte[] fileContent = "test content".getBytes();
        cache.put("file.txt", fileContent);

        assertThat(cache.contains("file.txt")).isTrue();
    }

    @Test
    void testContainsReturnsFalseForNonExistentKey() {
        assertThat(cache.contains("nonexistent.html")).isFalse();
    }

    @Test
    void testGetReturnsNullForNonExistentKey() {
        byte[] result = cache.get("missing.txt");
        assertThat(result).isNull();
    }

    @Test
    void testMultipleFiles() {
        byte[] content1 = "Content 1".getBytes();
        byte[] content2 = "Content 2".getBytes();
        byte[] content3 = "Content 3".getBytes();

        cache.put("file1.html", content1);
        cache.put("file2.css", content2);
        cache.put("file3.js", content3);

        assertThat(cache.get("file1.html")).isEqualTo(content1);
        assertThat(cache.get("file2.css")).isEqualTo(content2);
        assertThat(cache.get("file3.js")).isEqualTo(content3);
    }

    @Test
    void testClear() {
        cache.put("file1.html", "content1".getBytes());
        cache.put("file2.html", "content2".getBytes());
        assertThat(cache.size()).isEqualTo(2);

        cache.clear();

        assertThat(cache.size()).isEqualTo(0);
        assertThat(cache.contains("file1.html")).isFalse();
        assertThat(cache.contains("file2.html")).isFalse();
    }

    @Test
    void testSize() {
        assertThat(cache.size()).isEqualTo(0);

        cache.put("file1.html", "content".getBytes());
        assertThat(cache.size()).isEqualTo(1);

        cache.put("file2.html", "content".getBytes());
        assertThat(cache.size()).isEqualTo(2);

        cache.put("file3.html", "content".getBytes());
        assertThat(cache.size()).isEqualTo(3);
    }

    @Test
    void testOverwriteExistingKey() {
        byte[] oldContent = "old".getBytes();
        byte[] newContent = "new".getBytes();

        cache.put("file.html", oldContent);
        assertThat(cache.get("file.html")).isEqualTo(oldContent);

        cache.put("file.html", newContent);
        assertThat(cache.get("file.html")).isEqualTo(newContent);
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void testLargeFileContent() {
        byte[] largeContent = new byte[10000];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        cache.put("large.bin", largeContent);
        assertThat(cache.get("large.bin")).isEqualTo(largeContent);
    }

    @Test
    void testEmptyByteArray() {
        byte[] emptyContent = new byte[0];
        cache.put("empty.txt", emptyContent);

        assertThat(cache.contains("empty.txt")).isTrue();
        assertThat(cache.get("empty.txt")).isEmpty();
    }
}
