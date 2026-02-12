package org.example;

public class App {
    public static void main(String[] args) {
        int port = ServerPortResolver.resolvePort(args);
        new TcpServer(port).start();
    }
}