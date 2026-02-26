package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpServer {

    // Använder Logger istället för System.out
    private static final Logger logger = Logger.getLogger(TcpServer.class.getName());

    private final int port;
    private final ConnectionFactory connectionFactory;

    public TcpServer(int port, ConnectionFactory connectionFactory) {
        this.port = port;
        this.connectionFactory = connectionFactory;
    }

    public void start() {
        logger.log(Level.INFO, "Starting TCP server on port {0}", port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();

                // Lambda för att skjuta upp strängbygget
                logger.log(Level.INFO, () -> "Client connected: " + clientSocket.getRemoteSocketAddress());

                try {
                    clientSocket.setSoTimeout(10000);
                    Thread.ofVirtual().start(() -> handleClient(clientSocket));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Could not start thread for client", e);
                    closeQuietly(clientSocket);
                }
            }
        } catch (IOException e) {
            // Kastar IllegalStateException istället för generic RuntimeException
            throw new IllegalStateException("Server socket failed unexpectedly", e);
        }
    }

    protected void handleClient(Socket client) {
        try (client) {
            processRequest(client);
        } catch (IOException e) {
            logger.log(Level.WARNING, () -> "Network error with client: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error handling client", e);
        }
    }

    private void processRequest(Socket client) {
        //  Använder try-with-resources på ConnectionHandler (AutoCloseable)
        try (ConnectionHandler handler = connectionFactory.create(client)) {
            handler.runConnectionHandler();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to process request", e);
            handleInternalServerError(client);
        }
    }

    private void handleInternalServerError(Socket client) {
        if (client.isClosed()) return;

        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(HttpResponseBuilder.SC_INTERNAL_SERVER_ERROR);
        response.setHeaders(Map.of("Content-Type", "text/plain; charset=utf-8"));
        response.setBody("⚠️ Internal Server Error 500 ⚠️");

        try {
            OutputStream out = client.getOutputStream();
            out.write(response.build());
            out.flush();
        } catch (IOException _) {
            // Unnamed pattern (_)
        }
    }

    private void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException _) {
                // Fix: Unnamed pattern (_)
            }
        }
    }
}