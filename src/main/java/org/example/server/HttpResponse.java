package org.example.server;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpResponse {
    private int status = 200;
    private final Map<String, String> headers = new LinkedHashMap<>();

    public int status() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, String> headers() {
        return Collections.unmodifiableMap(headers);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }
}
