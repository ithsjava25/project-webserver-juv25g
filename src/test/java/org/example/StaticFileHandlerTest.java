package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.example.http.HttpResponseBuilder.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class StaticFileHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void test_file_that_exists_should_return_200() throws IOException {
        // Arrange
        Files.writeString(tempDir.resolve("test.html"), "Hello Test");
        StaticFileHandler handler = new StaticFileHandler(tempDir.toString());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Act
        handler.sendGetRequest(output, "test.html");

        // Assert
        String response = output.toString();
        assertTrue(response.contains("HTTP/1.1 " + SC_OK + " OK"));
        assertTrue(response.contains("Hello Test"));
        assertTrue(response.contains("Content-Type: text/html; charset=UTF-8"));
    }

    @Test
    void test_file_that_does_not_exist_should_return_404() throws IOException {
        // Arrange
        Files.writeString(tempDir.resolve("pageNotFound.html"), "Not Found");
        StaticFileHandler handler = new StaticFileHandler(tempDir.toString());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Act
        handler.sendGetRequest(output, "missing.html");

        // Assert
        String response = output.toString();
        assertTrue(response.contains("HTTP/1.1 " + SC_NOT_FOUND + " Not Found"));
    }

    @Test
    void test_path_traversal_should_return_403() throws IOException {
        // Arrange
        Path webRoot = tempDir.resolve("www");
        Files.createDirectories(webRoot);
        Files.writeString(tempDir.resolve("secret.txt"), "SECRET");
        Files.writeString(webRoot.resolve("index.html"), "Public");
        
        StaticFileHandler handler = new StaticFileHandler(webRoot.toString());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Act
        handler.sendGetRequest(output, "../secret.txt");

        // Assert
        String response = output.toString();
        assertFalse(response.contains("SECRET"));
        assertTrue(response.contains("HTTP/1.1 " + SC_FORBIDDEN + " Forbidden"));
    }

    @ParameterizedTest
    @CsvSource({
            "index.html?foo=bar",
            "index.html#section",
            "/index.html"
    })
    void sanitized_uris_should_return_200(String uri) throws IOException {
        // Arrange
        Files.writeString(tempDir.resolve("index.html"), "Home");
        StaticFileHandler handler = new StaticFileHandler(tempDir.toString());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Act
        handler.sendGetRequest(output, uri);

        // Assert
        assertTrue(output.toString().contains("HTTP/1.1 " + SC_OK + " OK"));
    }

    @Test
    void null_byte_injection_should_return_403() throws IOException {
        // Arrange
        StaticFileHandler handler = new StaticFileHandler(tempDir.toString());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Act
        handler.sendGetRequest(output, "index.html\0../../etc/passwd");

        // Assert
        String response = output.toString();
        assertFalse(response.contains("OK"));
        assertTrue(response.contains("HTTP/1.1 " + SC_FORBIDDEN + " Forbidden"));

    }

    @Test
    void small_file_should_be_loaded_in_memory() throws IOException {
        // Arrange - 500KB fil (under 1MB threshold)
        byte[] smallContent = new byte[500 * 1024];
        for (int i = 0; i < smallContent.length; i++) {
            smallContent[i] = (byte) (i % 256);
        }
        Files.write(tempDir.resolve("small.bin"), smallContent);
        
        StaticFileHandler handler = new StaticFileHandler(tempDir.toString());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Act
        handler.sendGetRequest(output, "small.bin");

        // Assert - Bör innehålla hela filen
        String response = output.toString();
        assertTrue(response.contains("HTTP/1.1 " + SC_OK + " OK"));
        assertTrue(response.contains("Content-Length: 512000"));
    }

    @Test
    void large_file_should_be_streamed() throws IOException {
        // Arrange - 2MB fil (över 1MB threshold)
        byte[] largeContent = new byte[2 * 1024 * 1024];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        Files.write(tempDir.resolve("large.bin"), largeContent);
        
        StaticFileHandler handler = new StaticFileHandler(tempDir.toString());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Act - Bör inte kasta OutOfMemoryError
        assertDoesNotThrow(() -> handler.sendGetRequest(output, "large.bin"));

        // Assert
        String response = output.toString();
        assertTrue(response.contains("HTTP/1.1 " + SC_OK + " OK"));
        assertTrue(response.contains("Content-Length: " + largeContent.length));
    }

    @Test
    void large_file_does_not_load_entire_content_in_memory() throws IOException {
        // Arrange - 5MB fil för att testa streaming utan att crasha
        byte[] largeContent = new byte[5 * 1024 * 1024]; // 5MB
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        Files.write(tempDir.resolve("streaming.bin"), largeContent);
        
        StaticFileHandler handler = new StaticFileHandler(tempDir.toString());
        
        // Använd NullOutputStream för att ej buffra allt i minnet
        NullOutputStream nullOut = new NullOutputStream();

        // Act - Bör inte kastas OutOfMemoryError
        assertDoesNotThrow(() -> handler.sendGetRequest(nullOut, "streaming.bin"));
    }

    /**
     * Dummy OutputStream som slänger allt - för att testa streaming utan minnesöverbelastning
     */
    private static class NullOutputStream extends java.io.OutputStream {
        @Override
        public void write(int b) throws IOException {
            // Discard
        }

        @Override
        public void write(byte[] b) throws IOException {
            // Discard
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            // Discard
        }
    }

    private int findHeaderBodySeparator(byte[] data) {
        // Sök efter \r\n\r\n (0x0D 0x0A 0x0D 0x0A)
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' &&
                    data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

}
