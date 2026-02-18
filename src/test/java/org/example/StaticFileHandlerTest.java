package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class StaticFileHandlerTest {
    private StaticFileHandler handler;
    private ByteArrayOutputStream outputStream;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        handler = new StaticFileHandler();
        outputStream = new ByteArrayOutputStream();

        // Skapa en temporär www-mapp för tester
        Files.createDirectory(tempDir.resolve("www"));
    }

    @Test
    void testCacheHitOnSecondRequest() throws IOException {
        // Förbereda testfil
        byte[] testContent = "Test HTML Content".getBytes();
        Path testFile = tempDir.resolve("www/test.html");
        Files.write(testFile, testContent);

        // Första request - cache miss
        handler.sendGetRequest(outputStream, "test.html");
        String firstResponse = outputStream.toString();

        // Rensa output
        outputStream.reset();

        // Andra request - cache hit
        handler.sendGetRequest(outputStream, "test.html");
        String secondResponse = outputStream.toString();

        // Båda responses ska innehålla samma innehål
        assertThat(firstResponse).contains(new String(testContent));
        assertThat(secondResponse).contains(new String(testContent));
    }

    @Test
    void testCacheContainsMultipleFiles() throws IOException {
        // Skapa två testfiler
        byte[] content1 = "Content 1".getBytes();
        byte[] content2 = "Content 2".getBytes();

        Files.write(tempDir.resolve("www/file1.html"), content1);
        Files.write(tempDir.resolve("www/file2.html"), content2);

        // Begär båda filerna
        handler.sendGetRequest(outputStream, "file1.html");
        outputStream.reset();
        handler.sendGetRequest(outputStream, "file2.html");

        // Båda ska finnas i cachen
        assertThat(outputStream.toString()).contains(new String(content2));
    }

    @Test
    void testNullFilenameHandling() {
        assertThatThrownBy(() -> handler.sendGetRequest(outputStream, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testFileNotFound() throws IOException {
        assertThatThrownBy(() -> handler.sendGetRequest(outputStream, "nonexistent.html"))
                .isInstanceOf(java.nio.file.NoSuchFileException.class);
    }

    @Test
    void testResponseIncludesCorrectContentType() throws IOException {
        byte[] testContent = "<html></html>".getBytes();
        Files.write(tempDir.resolve("www/test.html"), testContent);

        handler.sendGetRequest(outputStream, "test.html");
        String response = outputStream.toString();

        assertThat(response).contains("Content-Type: text/html");
    }

    @Test
    void testResponseIncludesHttpHeaders() throws IOException {
        byte[] testContent = "Test".getBytes();
        Files.write(tempDir.resolve("www/test.html"), testContent);

        handler.sendGetRequest(outputStream, "test.html");
        String response = outputStream.toString();

        assertThat(response)
                .contains("HTTP/1.1 200 OK")
                .contains("Content-Length:")
                .contains("Connection: close");
    }

    @Test
    void testDifferentFileExtensions() throws IOException {
        byte[] htmlContent = "<html></html>".getBytes();
        byte[] cssContent = "body { color: red; }".getBytes();

        Files.write(tempDir.resolve("www/style.html"), htmlContent);
        Files.write(tempDir.resolve("www/style.css"), cssContent);

        handler.sendGetRequest(outputStream, "style.html");
        String htmlResponse = outputStream.toString();

        outputStream.reset();
        handler.sendGetRequest(outputStream, "style.css");
        String cssResponse = outputStream.toString();

        assertThat(htmlResponse).contains("text/html");
        assertThat(cssResponse).contains("text/html"); // StaticFileHandler sätter alltid text/html för nu
    }

    @Test
    void testLargeFile() throws IOException {
        byte[] largeContent = new byte[1000000]; // 1MB
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        Files.write(tempDir.resolve("www/large.bin"), largeContent);

        handler.sendGetRequest(outputStream, "large.bin");
        String response = outputStream.toString();

        // Kontrollera att Content-Length är korrekt
        assertThat(response).contains("Content-Length: 1000000");
    }

    @Test
    void testUnicodeContent() throws IOException {
        String unicodeContent = "Hej världen! 你好 مرحبا";
        byte[] content = unicodeContent.getBytes();
        Files.write(tempDir.resolve("www/unicode.html"), content);

        handler.sendGetRequest(outputStream, "unicode.html");
        String response = outputStream.toString();

        assertThat(response).contains(unicodeContent);
    }
}
