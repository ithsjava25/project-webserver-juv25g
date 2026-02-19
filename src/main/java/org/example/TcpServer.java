package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class TcpServer {

    private final int port;

    public TcpServer(int port) {
        this.port = port;
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

    private void handleClient(Socket client) {
        try (ConnectionHandler connectionHandler = new ConnectionHandler(client)) {
            connectionHandler.runConnectionHandler();
        } catch (Exception e) {
            handleInternalServerError(client);
        }
    }

    private void handleInternalServerError(Socket client){
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(500);
        response.setHeaders(Map.of("Content-Type", "text/plain; charset=utf-8"));
        response.setBody("⚠️ Internal Server Error 500 ⚠️");

        try(PrintWriter writer = new PrintWriter(client.getOutputStream(), true)) {
            writer.print(response.build());
        } catch (IOException e) {
            throw new RuntimeException("Failed to print error message", e);
        }
    }
}
