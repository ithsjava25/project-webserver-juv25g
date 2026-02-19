package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionHandlerTest {

    @Mock
    private Socket socket;

    @TempDir
    Path tempDir;

    @Test
    void test_jpg_file_should_return_200_not_404() throws Exception {
        // Arrange
        byte[] imageContent = "fake-image-data".getBytes(StandardCharsets.UTF_8);
        Path imagePath = tempDir.resolve("test.jpg");
        Files.write(imagePath, imageContent);

        String request = "GET /test.jpg HTTP/1.1\r\nHost: localhost\r\n\r\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);

        // Act
        try (ConnectionHandler handler = new ConnectionHandler(socket, tempDir.toString())) {
            handler.runConnectionHandler();
        }

        // Assert
        String response = outputStream.toString();

        assertThat(response).contains("HTTP/1.1 200 OK");
        assertThat(response).doesNotContain("404");
    }
}