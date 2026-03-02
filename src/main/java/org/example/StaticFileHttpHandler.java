package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class StaticFileHttpHandler implements HttpHandler {

    private final StaticFileHandler handler;

    public StaticFileHttpHandler() {
        this.handler = new StaticFileHandler();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] fileBytes;
        int statusCode;
        String path = exchange.getRequestURI().getPath();

        if (path == null || path.isEmpty() || "/".equals(path)) {
            path = "index.html";
        } else if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                handler.handleGetRequest(path);
                fileBytes = handler.getFileBytes();
                statusCode = handler.getStatusCode();
            } catch (IOException e) {
                fileBytes = "500 Internal Server Error".getBytes();
                statusCode = 500;
            }

            exchange.sendResponseHeaders(statusCode, fileBytes.length);
            exchange.getResponseBody().write(fileBytes);
        } else {
            fileBytes = "405 Method Not Allowed".getBytes();
            statusCode = 405;
            exchange.sendResponseHeaders(statusCode, fileBytes.length);
            exchange.getResponseBody().write(fileBytes);
        }

        exchange.close();
    }
}