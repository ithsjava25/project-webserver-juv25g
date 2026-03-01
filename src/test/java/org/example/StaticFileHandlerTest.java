package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test class for verifying the behavior of the StaticFileHandler class.
 *
 * This test class ensures that StaticFileHandler correctly handles GET requests
 * for static files, including both cases where the requested file exists and
 * where it does not. Temporary directories and files are utilized in tests to
 * ensure no actual file system dependencies during test execution.
 *
 * Key functional aspects being tested include:
 * - Correct response status code and content for an existing file.
 * - Correct response status code and fallback behavior for a missing file.
 */
class StaticFileHandlerTest {

    @TempDir
    Path tempDir;

    private FileCache cache;

    @BeforeEach
    void setUp() {
        cache = new FileCache(10);
    }

    private StaticFileHandler createHandler(){
        return new StaticFileHandler(tempDir.toString(), cache);
    }

    private String sendRequest(String uri) throws IOException {
        StaticFileHandler handler = createHandler();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.sendGetRequest(output, uri);
        return output.toString();
    }

    @Test
    void testCaching_HitOnSecondRequest() throws IOException {
        // Arrange
        Files.writeString(tempDir.resolve("cached.html"), "Content");

        // Act
        String response1 = sendRequest("cached.html");
        String response2 = sendRequest("cached.html");

        // Assert
        assertThat(response1).contains("HTTP/1.1 200");
        assertThat(response2).contains("HTTP/1.1 200");
    }


    @Test
    void testSanitization_QueryString() throws IOException {
        Files.writeString(tempDir.resolve("index.html"), "Home");
        assertThat(sendRequest("index.html?foo=bar")).contains("HTTP/1.1 200");
    }

    @Test
    void testSanitization_LeadingSlash() throws IOException {
        Files.writeString(tempDir.resolve("page.html"), "Page");
        assertThat(sendRequest("/page.html")).contains("HTTP/1.1 200");
    }

    @Test
    void testSanitization_NullBytes() {
        assertThrows(NoSuchFileException.class, () -> {
            sendRequest("file.html\0../../secret");
        });
    }

    @Test
    void testConcurrent_MultipleReads() throws InterruptedException, IOException {
        // Arrange
        Files.writeString(tempDir.resolve("shared.html"), "Data");
        StaticFileHandler handler = new StaticFileHandler(tempDir.toString(), cache);

        handler.sendGetRequest(new ByteArrayOutputStream(), "shared.html");

        // Act - 10 threads reading same file 50 times each
        Thread[] threads = new Thread[10];
        final Exception[] threadError = new Exception[1];

        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        handler.sendGetRequest(out, "shared.html");
                        String response = out.toString();

                        if (!response.contains("HTTP/1.1 200") || !response.contains("Data")) {
                            throw new AssertionError("Unexpected response");
                        }
                    }
                } catch (Exception e) {
                    synchronized (threads) {
                        threadError[0] = e;
                    }
                }
            });
            threads[i].start();
        }
    
    // Wait for all threads to complete
    for (Thread thread : threads) {
        thread.join();
    }
    
    // Assert
    assertNull(threadError[0], "Thread threw exception: " + (threadError[0] != null ? threadError[0].getMessage() : ""));
}

    @Test
    void test_file_that_exists_should_return_200() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.html");
        Files.writeString(testFile, "Hello Test");

        StaticFileHandler staticFileHandler = new StaticFileHandler(tempDir.toString(), cache);
        ByteArrayOutputStream fakeOutput = new ByteArrayOutputStream();

        // Act
        staticFileHandler.sendGetRequest(fakeOutput, "test.html");

        // Assert
        String response = fakeOutput.toString();
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Hello Test"));
        assertTrue(response.contains("Content-Type: text/html; charset=UTF-8"));
    }

    @Test
    void test_file_that_does_not_exists_should_throw_exception() {
        // Arrange
        StaticFileHandler staticFileHandler = new StaticFileHandler(tempDir.toString(), cache);
        ByteArrayOutputStream fakeOutput = new ByteArrayOutputStream();

        // Act & Assert
        assertThrows(NoSuchFileException.class, () -> {
            staticFileHandler.sendGetRequest(fakeOutput, "notExistingFile.html");
        });
    }

    @Test
    void null_byte_injection_should_throw_exception() throws IOException {
        // Arrange
        Path webRoot = tempDir.resolve("www");
        Files.createDirectories(webRoot);
        StaticFileHandler handler = new StaticFileHandler(webRoot.toString(), cache);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Act & Assert
        assertThrows(NoSuchFileException.class, () -> {
            handler.sendGetRequest(out, "index.html\0../../etc/passwd");
        });
    }

    @Test
    void testMaxCacheSize() throws IOException {
        FileCache limitedCache = new FileCache(2);
        StaticFileHandler handler = new StaticFileHandler(tempDir.toString(), limitedCache);

        Files.writeString(tempDir.resolve("1.html"), "1");
        Files.writeString(tempDir.resolve("2.html"), "2");
        Files.writeString(tempDir.resolve("3.html"), "3");

        handler.sendGetRequest(new ByteArrayOutputStream(), "1.html");
        handler.sendGetRequest(new ByteArrayOutputStream(), "2.html");
        assertEquals(2, limitedCache.size());

        handler.sendGetRequest(new ByteArrayOutputStream(), "3.html");
        assertEquals(2, limitedCache.size());

        // 1.html bör ha tagits bort eftersom det var äldst (LRU)
        assertNull(limitedCache.get(tempDir.resolve("1.html").toString()));
        assertNotNull(limitedCache.get(tempDir.resolve("2.html").toString()));
        assertNotNull(limitedCache.get(tempDir.resolve("3.html").toString()));
    }
}
