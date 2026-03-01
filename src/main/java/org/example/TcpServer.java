package org.example;

import org.example.http.HttpResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class TcpServer {

    private final int port;
    private final ConnectionFactory connectionFactory;
    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    public TcpServer(int port, ConnectionFactory connectionFactory) {
        this.port = port;
        this.connectionFactory = connectionFactory;
    }

    public void start() {
        log.info("Starting TCP server on port {}", port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // block
                log.info("Client connected");
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
            log.error("Failed when handling connection to client", e);
        }
    }

    /*
    The connection handler must be run in a try-catch-finally. Otherwise - if we use try-with-resources - the
    connection will always be closed when code reaches handleInternalServerError() and there will never
    be an error print on the client's webpage.
     */
    private void processRequest(Socket client) throws Exception {
        ConnectionHandler handler = null;
        try{
            handler = connectionFactory.create(client);
            handler.runConnectionHandler();
        } catch (Exception e) {
            log.error("Internal Server Error", e);
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
                log.error("Client disconnected before 500 response could be sent", e);
            }
        }
    }
}
