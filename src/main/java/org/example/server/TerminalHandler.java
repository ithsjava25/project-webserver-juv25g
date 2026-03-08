package org.example.server;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

public interface TerminalHandler {
    void handle(HttpRequest request, HttpResponseBuilder responseBuilder);
}
