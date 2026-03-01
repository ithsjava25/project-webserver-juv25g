package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

    private final int port;
    private final ConnectionFactory connectionFactory;

    public TcpServer(int port, ConnectionFactory connectionFactory) {
        this.port = port;
        this.connectionFactory = connectionFactory;
    }

    public void start() {
        System.out.println("Starting TCP server on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // block
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
                Thread.ofVirtual().start(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to start TCP server", e);
        }
    }

    protected void handleClient(Socket client) {
        try(client){
            processRequest(client);
        } catch (Exception e) {
            throw new RuntimeException("Failed to close socket", e);
        }
    }

    private void processRequest(Socket client) throws Exception {
        try (ConnectionHandler handler = connectionFactory.create(client)) {
            handler.runConnectionHandler();
        } catch (Exception e) {
            handleInternalServerError(client);
        }
    }


    private void handleInternalServerError(Socket client){
        if (!client.isClosed()) {
            try {
                OutputStream out = client.getOutputStream();
                out.write(HttpResponseBuilder.createErrorResponse(500, "Internal Server Error"));
                out.flush();
            } catch (IOException e) {
                System.err.println("Failed to send 500 response: " + e.getMessage());
            }
        }
    }
}
