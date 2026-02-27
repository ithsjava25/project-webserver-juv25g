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
    void failedClientRequestShouldReturnError500() throws Exception {
        ConnectionFactory mockFactory = Mockito.mock(ConnectionFactory.class);
        ConnectionHandler mockHandler = Mockito.mock(ConnectionHandler.class);
        TcpServer server = new TcpServer(0, mockFactory);

        Socket mockSocket = Mockito.mock(Socket.class);
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

        // Setup mocks
        when(mockSocket.getOutputStream()).thenReturn(outputStream);
        when(mockSocket.isConnected()).thenReturn(true);
        when(mockSocket.isClosed()).thenReturn(false);
        when(mockFactory.create(any(Socket.class))).thenReturn(mockHandler);

        // Simulera krasch i handlern
        Mockito.doThrow(new RuntimeException("Simulated Crash"))
                .when(mockHandler).runConnectionHandler();

        // Kör metoden
        server.handleClient(mockSocket);

        // Konvertera output till sträng (använd UTF-8 för att vara säker)
        String response = outputStream.toString(java.nio.charset.StandardCharsets.UTF_8);

        // Logga gärna ut vad responsen faktiskt innehåller om det fortsätter strula:
        // System.out.println("Actual Response: " + response);

        assertAll(
                // Vi kollar efter delar av HTTP-statusraden och bodyn
                () -> assertTrue(response.contains("500"), "Response should contain status code 500"),
                () -> assertTrue(response.contains("Internal Server Error"), "Response should contain error message"),
                () -> assertTrue(response.contains("Content-Type"), "Response should contain Content-Type header")
        );
    }
}
