package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class TcpServer {

    private final int port;
    private final ConnectionFactory connectionFactory;

    public TcpServer(int port, ConnectionFactory connectionFactory) {
        this.port = port;
        this.connectionFactory = connectionFactory;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();

                try {
                    clientSocket.setSoTimeout(10000);
                    Thread.ofVirtual().start(() -> handleClient(clientSocket));
                } catch (Exception _) {
                    closeQuietly(clientSocket);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("TCP Server failed on port " + port, e);
        }
    }

    protected void handleClient(Socket client) {
        try (client) {
            processRequest(client);
        } catch (Exception _) {
            // Ska fyllas in med nya pr
        }
    }

    private void processRequest(Socket client) {
        ConnectionHandler handler = null;
        try {
            handler = connectionFactory.create(client);
            handler.runConnectionHandler();
        } catch (Exception _) {
            handleInternalServerError(client);
        } finally {
            if (handler != null) {
                try {
                    handler.close();
                } catch (Exception _) {
                    // ska fyllas in med nya PR
                }
            }
        }
    }

    private void handleInternalServerError(Socket client) {
        // Kontrollera att vi kan skriva till klienten
        if (client.isClosed() || !client.isConnected() || client.isOutputShutdown()) {
            return;
        }

        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(HttpResponseBuilder.SC_INTERNAL_SERVER_ERROR);
        response.setHeaders(Map.of("Content-Type", "text/plain; charset=utf-8"));
        response.setBody("⚠️ Internal Server Error 500 ⚠️");

        try {
            OutputStream out = client.getOutputStream();
            out.write(response.build());
            out.flush();
        } catch (IOException _) {
            // Ignorera nätverksfel vid sändning av felmeddelandet
        }
    }

    private void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException _) {
                // Tyst stängning
            }
        }
    }
}