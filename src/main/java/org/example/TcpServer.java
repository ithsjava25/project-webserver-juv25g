package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
            //  Tillåt avbrott genom timeout
            serverSocket.setSoTimeout(1000);

            while (!Thread.currentThread().isInterrupted()) {
                acceptAndHandleClient(serverSocket);
            }
        } catch (IOException e) {
            throw new IllegalStateException("TCP Server failed on port " + port, e);
        }
    }

    private void acceptAndHandleClient(ServerSocket serverSocket) {
        try {
            Socket clientSocket = serverSocket.accept();
            startClientTask(clientSocket);
        } catch (SocketTimeoutException _) {
            // Normal timeout för att checka interrupt-flaggan
        } catch (IOException _) {
            // Will be logged with when new system is integrated
        }
    }

    private void startClientTask(Socket clientSocket) {
        try {
            clientSocket.setSoTimeout(10000);
            Thread.ofVirtual().start(() -> handleClient(clientSocket));
        } catch (Exception _) {
            // Om tråden inte kan startas, stäng socketen direkt
            closeQuietly(clientSocket);
        }
    }

    protected void handleClient(Socket client) {
        try (client) {
            processRequest(client);
        } catch (Exception _) {
            // Plats för framtida loggning
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
            closeHandler(handler);
        }
    }

    private void handleInternalServerError(Socket client) {
        // Fix för CodeRabbit: Dubbelkolla att output inte är stängd
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
            // Plats för framtida loggning
        }
    }

    private void closeHandler(ConnectionHandler handler) {
        if (handler != null) {
            try {
                handler.close();
            } catch (Exception _) {
                // Tyst stängning av handler
            }
        }
    }

    private void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException _) {
                // Tyst stängning av socket
            }
        }
    }
}