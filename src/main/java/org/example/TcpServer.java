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
                    // Om tråden inte kan starta, stäng direkt för att inte läcka
                    closeQuietly(clientSocket);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("TCP Server failed on port " + port, e);
        }
    }

    protected void handleClient(Socket client) {
        try {
            // Kör logiken först
            processRequest(client);
        } catch (Exception _) {
            // Eventuella oväntade fel fångas här (loggas här med nya system)
        } finally {
            closeQuietly(client);
        }
    }

    private void processRequest(Socket client) {
        ConnectionHandler handler = null;
        try {
            // Skapa handlern manuellt (ingen try-with-resources här heller)
            handler = connectionFactory.create(client);
            handler.runConnectionHandler();
        } catch (Exception _) {
            // 1. Logga felet (loggning system)

            // 2. Skicka 500-svar (Socketen är fortfarande öppen här!)
            handleInternalServerError(client);
        } finally {
            // 3. Stäng handlern manuellt
            if (handler != null) {
                try {
                    handler.close();
                } catch (Exception _) {
                    // Ignorera fel vid stängning av handlern
                }
            }
        }
    }

    private void handleInternalServerError(Socket client) {
        // Kolla om vi kan prata med klienten
        if (client.isClosed() || !client.isConnected()) {
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
            // Ignorera nätverksfel vid sändning
        }
    }

    private void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException _) {
                // Tyst stängning för att förhindra krasch under felhantering
            }
        }
    }
}