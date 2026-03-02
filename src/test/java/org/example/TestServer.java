package org.example;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class TestServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new StaticFileHttpHandler());
        server.start();
        System.out.println("Server started on http://localhost:8080");
    }
}