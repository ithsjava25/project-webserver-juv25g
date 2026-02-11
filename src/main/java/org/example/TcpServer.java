package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
                clientSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to start TCP server", e);
        }
    }
}