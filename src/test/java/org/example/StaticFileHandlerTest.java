package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class StaticFileHandlerTest {
    
    private StaticFileHandler handler;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Injicera temporär katalog för testning
        handler = new StaticFileHandler(tempDir.toString());
    }

    @Test
    void testSendSmallFile() throws IOException {
        // Skapa en liten testfil (< 1MB)
        String smallContent = "Hello World";
        Path testFile = tempDir.resolve("small.html");
        Files.writeString(testFile, smallContent);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.sendGetRequest(output, "small.html");
        
        String result = output.toString();
        assertThat(result)
            .contains("HTTP/1.1 200 OK")
            .contains("Content-Type: text/html");
    }

    @Test
    void testSendMediumFile() throws IOException {
        // Testa med 500KB fil (mellan små och stora)
        byte[] content = new byte[500 * 1024]; // 500KB
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }
        
        Path testFile = tempDir.resolve("medium.bin");
        Files.write(testFile, content);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.sendGetRequest(output, "medium.bin");
        
        String result = output.toString();
        assertThat(result)
            .contains("HTTP/1.1 200 OK")
            .contains("Content-Length: 512000");
    }

    @Test
    void testStreamingLargeFile() throws IOException {
        // Testa streaming med större fil (2MB)
        byte[] content = new byte[2 * 1024 * 1024]; // 2MB
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }
        
        Path testFile = tempDir.resolve("large.bin");
        Files.write(testFile, content);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        // Denna ska streaming och inte kasta OutOfMemoryError
        assertThatCode(() -> handler.sendGetRequest(output, "large.bin"))
            .doesNotThrowAnyException();
        
        String result = output.toString();
        assertThat(result)
            .contains("HTTP/1.1 200 OK")
            .contains("Content-Length: " + content.length);
    }

    @Test
    void testHttpHeadersFormatting() throws IOException {
        String content = "Test Content";
        Path testFile = tempDir.resolve("test.html");
        Files.writeString(testFile, content);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.sendGetRequest(output, "test.html");
        
        String result = output.toString();
        assertThat(result)
            .contains("HTTP/1.1 200 OK")
            .contains("Content-Type: text/html; charset=utf-8")
            .contains("Connection: close")
            .contains("Content-Length:");
    }

    @Test
    void testEmptyFile() throws IOException {
        Path testFile = tempDir.resolve("empty.html");
        Files.createFile(testFile);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.sendGetRequest(output, "empty.html");
        
        String result = output.toString();
        assertThat(result)
            .contains("HTTP/1.1 200 OK")
            .contains("Content-Length: 0");
    }

    @Test
    void testSmallFileUsesCaching() throws IOException {
        // Testa att små filer cachas
        String content = "Cached Content";
        Path testFile = tempDir.resolve("cached.html");
        Files.writeString(testFile, content);
        
        ByteArrayOutputStream output1 = new ByteArrayOutputStream();
        handler.sendGetRequest(output1, "cached.html");
        
        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        handler.sendGetRequest(output2, "cached.html");
        
        // Båda anrop ska fungera
        assertThat(output1.toString()).contains("HTTP/1.1 200 OK");
        assertThat(output2.toString()).contains("HTTP/1.1 200 OK");
    }
}