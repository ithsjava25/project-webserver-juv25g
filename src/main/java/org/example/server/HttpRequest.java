package org.example.server;

import java.util.Objects;

public final class HttpRequest {
    private final String method;
    private final String path;

    public HttpRequest(String method, String path) {
        this.method = Objects.requireNonNull(method, "method");
        this.path = Objects.requireNonNull(path, "path");
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }
}
