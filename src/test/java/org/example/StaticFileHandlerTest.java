package org.example;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.example.http.HttpResponseBuilder.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for verifying the behavior of the StaticFileHandler class.
 * <p>
 * This test class ensures that StaticFileHandler correctly handles GET requests
 * for static files, including both cases where the requested file exists and
 * where it does not. Temporary directories and files are utilized in tests to
 * ensure no actual file system dependencies during test execution.
 * <p>
 * Key functional aspects being tested include:
 * - Correct response status code and content for an existing file.
 * - Correct response status code and fallback behavior for a missing file.
 */
class StaticFileHandlerTest {

    //Junit creates a temporary folder which can be filled with temporary files that gets removed after tests
    @TempDir
    Path tempDir;


    @Test
    void test_file_that_exists_should_return_200() throws IOException {
        //Arrange
        Path testFile = tempDir.resolve("test.html"); // Defines the path in the temp directory
        Files.writeString(testFile, "Hello Test"); // Creates a text in that file

        //Using the new constructor in StaticFileHandler to reroute so the tests uses the temporary folder instead of the hardcoded www
        StaticFileHandler staticFileHandler = new StaticFileHandler(tempDir.toString());

        HttpRequest request = new HttpRequest("GET", "test.html", "HTTP/1.1", Collections.emptyMap(), "");
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();

        //Act
        staticFileHandler.handle(request, responseBuilder); //Get test.html and update the responseBuilder

        //Assert
        assertEquals(SC_OK, responseBuilder.getStatusCode());
        assertEquals("text/html; charset=UTF-8", responseBuilder.getHeader("Content-Type"));
        // Check body. Note: responseBuilder.setBody(bytes) sets bytebody.
        // getBodyBytes() returns bytebody if present.
        String body = new String(responseBuilder.getBodyBytes(), StandardCharsets.UTF_8);
        assertTrue(body.contains("Hello Test"));
    }

    @Test
    void test_file_that_does_not_exists_should_return_404() throws IOException {
        //Arrange
        // Pre-create the mandatory error page in the temp directory to prevent NoSuchFileException
        Path testFile = tempDir.resolve("pageNotFound.html");
        Files.writeString(testFile, "Fallback page");

        //Using the new constructor in StaticFileHandler to reroute so the tests uses the temporary folder instead of the hardcoded www
        StaticFileHandler staticFileHandler = new StaticFileHandler(tempDir.toString());

        HttpRequest request = new HttpRequest("GET", "notExistingFile.html", "HTTP/1.1", Collections.emptyMap(), "");
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();

        //Act
        staticFileHandler.handle(request, responseBuilder); // Request a file that clearly doesn't exist to trigger the 404 logic

        //Assert
        assertEquals(SC_NOT_FOUND, responseBuilder.getStatusCode());
    }

    @Test
    void test_path_traversal_should_return_403() throws IOException {
        // Arrange
        Path secret = tempDir.resolve("secret.txt");
        Files.writeString(secret, "TOP SECRET");
        Path webRoot = tempDir.resolve("www");
        Files.createDirectories(webRoot);
        StaticFileHandler handler = new StaticFileHandler(webRoot.toString());

        HttpRequest request = new HttpRequest("GET", "../secret.txt", "HTTP/1.1", Collections.emptyMap(), "");
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();

        // Act
        handler.handle(request, responseBuilder);

        // Assert
        assertEquals(SC_FORBIDDEN, responseBuilder.getStatusCode());
        String body = new String(responseBuilder.getBodyBytes(), StandardCharsets.UTF_8);
        assertFalse(body.contains("TOP SECRET"));
    }

    @ParameterizedTest
    @CsvSource({
            "index.html?foo=bar",
            "index.html#section",
            "/index.html"
    })
    void sanitized_uris_should_return_200(String uri) throws IOException {
        // Arrange
        Path webRoot = tempDir.resolve("www");
        Files.createDirectories(webRoot);
        Files.writeString(webRoot.resolve("index.html"), "Hello");
        StaticFileHandler handler = new StaticFileHandler(webRoot.toString());

        HttpRequest request = new HttpRequest("GET", uri, "HTTP/1.1", Collections.emptyMap(), "");
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();

        // Act
        handler.handle(request, responseBuilder);

        // Assert
        assertEquals(SC_OK, responseBuilder.getStatusCode());
    }

    @Test
    void null_byte_injection_should_not_return_200() throws IOException {
        // Arrange
        Path webRoot = tempDir.resolve("www");
        Files.createDirectories(webRoot);
        StaticFileHandler handler = new StaticFileHandler(webRoot.toString());

        HttpRequest request = new HttpRequest("GET", "index.html\0../../etc/passwd", "HTTP/1.1", Collections.emptyMap(), "");
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();

        // Act
        handler.handle(request, responseBuilder);

        // Assert
        assertNotEquals(SC_OK, responseBuilder.getStatusCode());
        assertEquals(SC_NOT_FOUND, responseBuilder.getStatusCode());
    }

    @Test
    void non_get_method_should_return_405() throws IOException {
        Path webRoot = tempDir.resolve("www");
        Files.createDirectories(webRoot);
        Files.writeString(webRoot.resolve("index.html"), "Hello");
        StaticFileHandler handler = new StaticFileHandler(webRoot.toString());

        HttpRequest request = new HttpRequest("POST", "index.html", "HTTP/1.1", Collections.emptyMap(), "");
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();

        handler.handle(request, responseBuilder);

        assertEquals(SC_METHOD_NOT_ALLOWED, responseBuilder.getStatusCode());
        assertEquals("text/plain; charset=utf-8", responseBuilder.getHeader("Content-Type"));
        assertEquals("GET", responseBuilder.getHeader("Allow"));
    }
}
