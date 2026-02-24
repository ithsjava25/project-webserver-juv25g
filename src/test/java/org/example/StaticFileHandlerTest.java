package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.example.http.HttpResponseBuilder.*;

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

        //Using ByteArrayOutputStream instead of Outputstream during tests to capture the servers response in memory, fake stream
        ByteArrayOutputStream fakeOutput = new ByteArrayOutputStream();

        //Act
        staticFileHandler.sendGetRequest(fakeOutput, "test.html"); //Get test.html and write the answer to fakeOutput

        //Assert
        String response = fakeOutput.toString();//Converts the captured byte stream into a String for verification

        assertTrue(response.contains("HTTP/1.1 " + SC_OK +  " OK")); // Assert the status
        assertTrue(response.contains("Hello Test")); //Assert the content in the file

        assertTrue(response.contains("Content-Type: text/html; charset=UTF-8")); // Verify the correct Content-type header

    }

    @Test
    void test_file_that_does_not_exists_should_return_404() throws IOException {
        //Arrange
        // Pre-create the mandatory error page in the temp directory to prevent NoSuchFileException
        Path testFile = tempDir.resolve("pageNotFound.html");
        Files.writeString(testFile, "Fallback page");

        //Using the new constructor in StaticFileHandler to reroute so the tests uses the temporary folder instead of the hardcoded www
        StaticFileHandler staticFileHandler = new StaticFileHandler(tempDir.toString());

        //Using ByteArrayOutputStream instead of Outputstream during tests to capture the servers response in memory, fake stream
        ByteArrayOutputStream fakeOutput = new ByteArrayOutputStream();

        //Act
        staticFileHandler.sendGetRequest(fakeOutput, "notExistingFile.html"); // Request a file that clearly doesn't exist to trigger the 404 logic

        //Assert
        String response = fakeOutput.toString();//Converts the captured byte stream into a String for verification

        assertTrue(response.contains("HTTP/1.1 " + SC_NOT_FOUND + " Not Found")); // Assert the status

    }

    @Test
    void test_path_traversal_should_return_403() throws IOException {
        // Arrange
        Path secret = tempDir.resolve("secret.txt");
        Files.writeString(secret,"TOP SECRET");
        Path webRoot = tempDir.resolve("www");
        Files.createDirectories(webRoot);
        StaticFileHandler handler = new StaticFileHandler(webRoot.toString());
        ByteArrayOutputStream fakeOutput = new ByteArrayOutputStream();

        // Act
        handler.sendGetRequest(fakeOutput, "../secret.txt");

        // Assert
        String response = fakeOutput.toString();
        assertFalse(response.contains("TOP SECRET"));
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
        Path webRoot = tempDir.resolve("www");
        Files.createDirectories(webRoot);
        Files.writeString(webRoot.resolve("index.html"), "Hello");
        StaticFileHandler handler = new StaticFileHandler(webRoot.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Act
        handler.sendGetRequest(out, uri);

        // Assert
        assertTrue(out.toString().contains("HTTP/1.1 " + SC_OK + " OK"));
    }

    @Test
    void null_byte_injection_should_not_return_200() throws IOException {
        // Arrange
        Path webRoot = tempDir.resolve("www");
        Files.createDirectories(webRoot);
        StaticFileHandler handler = new StaticFileHandler(webRoot.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Act
        handler.sendGetRequest(out, "index.html\0../../etc/passwd");

        // Assert
        String response = out.toString();
        assertFalse(response.contains("HTTP/1.1 " + SC_OK + " OK"));
        assertTrue(response.contains("HTTP/1.1 " + SC_NOT_FOUND +  " Not Found"));
    }
}
