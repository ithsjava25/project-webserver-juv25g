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
                    // VIKTIGT: Här stänger vi socketen om tråden dör,
                    // även om vi inte loggar lokalt.
                    closeQuietly(clientSocket);
                }
            }
        } catch (IOException e) {
            // SonarQube S112: Kasta specifikt exception istället för generic RuntimeException
            throw new IllegalStateException("TCP Server failed to remain open on port " + port, e);
        }
    }

    protected void handleClient(Socket client) {
        // try-with-resources garanterar att socket stängs
        try (client) {
            processRequest(client);
        } catch (Exception _) {
            // Felhantering sker i processRequest,
            // men vi fångar upp eventuella stängningsfel här.
        }
    }

    private void processRequest(Socket client) {
        // ConnectionHandler stängs automatiskt
        try (ConnectionHandler handler = connectionFactory.create(client)) {
            handler.runConnectionHandler();
        } catch (Exception _) {
            // Om något går snett i logiken skickar vi 500-svaret
            handleInternalServerError(client);
        }
    }

    private void handleInternalServerError(Socket client) {
        // Säkerhetsspärr: Skriv bara om socket lever
        if (client.isClosed() || !client.isConnected()) return;

        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(HttpResponseBuilder.SC_INTERNAL_SERVER_ERROR);
        response.setHeaders(Map.of("Content-Type", "text/plain; charset=utf-8"));
        response.setBody("⚠️ Internal Server Error 500 ⚠️");

        try {
            OutputStream out = client.getOutputStream();
            out.write(response.build());
            out.flush();
        } catch (IOException _) {
            // Unnamed pattern (_) - Java 21 standard för ignorerade fel
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