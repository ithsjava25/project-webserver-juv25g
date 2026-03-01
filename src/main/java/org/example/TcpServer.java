package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
        ConnectionHandler handler = null;
        try{
            handler = connectionFactory.create(client);
            handler.runConnectionHandler();
        } catch (Exception e) {
            handleInternalServerError(client);
        } finally {
            if(handler != null)
                handler.close();
        }
    }


    private void handleInternalServerError(Socket client){
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(HttpResponseBuilder.SC_INTERNAL_SERVER_ERROR);
        response.setHeaders(Map.of("Content-Type", "text/plain; charset=utf-8"));
        response.setBody("⚠️ Internal Server Error 500 ⚠️");

        if (!client.isClosed()) {
            try {
                OutputStream out = client.getOutputStream();
                out.write(response.build());
                out.flush();
            } catch (IOException e) {
                System.err.println("Failed to send 500 response: " + e.getMessage());
            }
        }
    }
}
