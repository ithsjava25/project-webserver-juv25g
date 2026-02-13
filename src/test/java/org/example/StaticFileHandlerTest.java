package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

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

        assertTrue(response.contains("HTTP/1.1 200 OK")); // Assert the status
        assertTrue(response.contains("Hello Test")); //Assert the content in the file



    }

}