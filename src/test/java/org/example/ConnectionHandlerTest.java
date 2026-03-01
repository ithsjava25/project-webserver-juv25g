package org.example;

import org.example.config.ConfigLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
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

    private final FileCache cache = new FileCache(10);

    @BeforeAll
    static void setupConfig() {
        ConfigLoader.resetForTests();
        ConfigLoader.loadOnce(Path.of("nonexistent-test-config.yml"));
    }

    @Test
    void test_jpg_file_should_return_200_not_404() throws Exception {
        // Arrange
        byte[] imageContent = "fake-image-data".getBytes(StandardCharsets.UTF_8);
        Files.write(tempDir.resolve("test.jpg"), imageContent);

        String request = "GET /test.jpg HTTP/1.1\r\nHost: localhost\r\n\r\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        // Act
        try (ConnectionHandler handler = new ConnectionHandler(socket, tempDir.toString(), cache)) {
            handler.runConnectionHandler();
        }

        // Assert
        String response = outputStream.toString();
        assertThat(response).contains("HTTP/1.1 200 OK");
        assertThat(response).doesNotContain("404");
    }

    @Test
    void test_root_path_should_serve_index_html() throws Exception {
        // Arrange
        byte[] indexContent = "<html><body>Hello</body></html>".getBytes(StandardCharsets.UTF_8);
        Files.write(tempDir.resolve("index.html"), indexContent);

        String request = "GET / HTTP/1.1\r\nHost: localhost\r\n\r\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        // Act
        try (ConnectionHandler handler = new ConnectionHandler(socket, tempDir.toString(), cache)) {
            handler.runConnectionHandler();
        }

        // Assert
        String response = outputStream.toString();
        assertThat(response).contains("HTTP/1.1 200 OK");
        assertThat(response).doesNotContain("404");
    }

    @Test
    void test_missing_file_should_return_404() throws Exception {
        // Arrange — no file written to tempDir
        String request = "GET /doesnotexist.html HTTP/1.1\r\nHost: localhost\r\n\r\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        // Act
        try (ConnectionHandler handler = new ConnectionHandler(socket, tempDir.toString(), cache)) {
            handler.runConnectionHandler();
        }

        // Assert
        String response = outputStream.toString();
        assertThat(response).contains("404");
    }

    @Test
    void test_path_traversal_should_return_403() throws Exception {
        // Arrange
        String request = "GET /../secret.txt HTTP/1.1\r\nHost: localhost\r\n\r\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        // Act
        try (ConnectionHandler handler = new ConnectionHandler(socket, tempDir.toString(), cache)) {
            handler.runConnectionHandler();
        }

        // Assert
        String response = outputStream.toString();
        assertThat(response).contains("403");
        assertThat(response).contains("Forbidden");
    }
}