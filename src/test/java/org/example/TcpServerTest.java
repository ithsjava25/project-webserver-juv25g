package org.example;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.Socket;

class TcpServerTest {


    @Test
    void failedClientRequestShouldReturnError500() throws Exception{
        ConnectionFactory mockFactory = Mockito.mock(ConnectionFactory.class);
        ConnectionHandler mockHandler = Mockito.mock(ConnectionHandler.class);
        TcpServer server = new TcpServer(0, mockFactory);

        Socket mockSocket = Mockito.mock(Socket.class);
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);
        when(mockFactory.create(any(Socket.class))).thenReturn(mockHandler);

        Mockito.doThrow(new RuntimeException("Simulated Crash"))
                .when(mockHandler).runConnectionHandler();

        server.handleClient(mockSocket);

        String response = outputStream.toString();
        assertAll(
                () -> assertTrue(response.contains("500")),
                () -> assertTrue(response.contains("Internal Server Error")),
                () -> assertTrue(response.contains("Content-Type: text/html"))
        );
    }
}
