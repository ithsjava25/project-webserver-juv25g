package org.example.server;

@FunctionalInterface
public interface TerminalHandler {
    void handle(HttpRequest request, HttpResponse response);
}