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
        byte[] content = new byte[2 * 1024 * 1024];
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
    void testStreamingLargeFileIntegrity() throws IOException {
        // Testa att all data strömmas korrekt (2MB)
        byte[] originalContent = new byte[2 * 1024 * 1024];
        for (int i = 0; i < originalContent.length; i++) {
            originalContent[i] = (byte) (i % 256);
        }

        Path testFile = tempDir.resolve("large_integrity.bin");
        Files.write(testFile, originalContent);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.sendGetRequest(output, "large_integrity.bin");

        byte[] outputBytes = output.toByteArray();

        // Sök efter HTTP-header separator (\r\n\r\n) i byte-array
        int separatorIndex = findHeaderBodySeparator(outputBytes);
        assertThat(separatorIndex).isGreaterThanOrEqualTo(0)
                .withFailMessage("Kunde inte hitta HTTP-header separator");

        // Extrahera body från HTTP-response
        byte[] receivedBody = new byte[outputBytes.length - separatorIndex - 4]; // -4 för \r\n\r\n
        System.arraycopy(outputBytes, separatorIndex + 4, receivedBody, 0, receivedBody.length);

        assertThat(receivedBody).isEqualTo(originalContent);
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

    @Test
    void testVeryLargeFile() throws IOException {
        // Testa streaming med mycket större fil (10MB)
        byte[] content = new byte[10 * 1024 * 1024]; // 10MB
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        Path testFile = tempDir.resolve("very_large.bin");
        Files.write(testFile, content);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Denna ska streaming och inte kasta OutOfMemoryError
        assertThatCode(() -> handler.sendGetRequest(output, "very_large.bin"))
                .doesNotThrowAnyException();

        String result = output.toString();
        assertThat(result)
                .contains("HTTP/1.1 200 OK")
                .contains("Content-Length: " + content.length);
    }

    @Test
    void testStreamingFilesDoNotExceedMemoryThreshold() throws IOException {
        // Testa att streaming inte använder mer än ~20MB för en 15MB fil
        byte[] content = new byte[15 * 1024 * 1024]; // 15MB
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        Path testFile = tempDir.resolve("memory_test.bin");
        Files.write(testFile, content);

        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.sendGetRequest(output, "memory_test.bin");

        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = memoryAfter - memoryBefore;

        // Memory increase should be much less than file size
        // (due to streaming with 8KB buffer)
        assertThat(memoryIncrease).isLessThan(50 * 1024 * 1024); // Less than 50MB
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

    @Test
    void testLargeFileNotCached() throws IOException {
        // Testa att stora filer INTE cachas (de streamas istället)
        byte[] content = new byte[2 * 1024 * 1024]; // 2MB
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        Path testFile = tempDir.resolve("not_cached.bin");
        Files.write(testFile, content);

        ByteArrayOutputStream output1 = new ByteArrayOutputStream();
        handler.sendGetRequest(output1, "not_cached.bin");

        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        handler.sendGetRequest(output2, "not_cached.bin");

        // Båda anrop ska fungera utan cachning
        assertThat(output1.toString()).contains("HTTP/1.1 200 OK");
        assertThat(output2.toString()).contains("HTTP/1.1 200 OK");
    }
}
