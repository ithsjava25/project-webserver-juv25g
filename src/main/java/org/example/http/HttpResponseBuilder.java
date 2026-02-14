package org.example.http;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class HttpResponseBuilder {

    private static final String PROTOCOL = "HTTP/1.1";
    private int statusCode = 200;
    private String body = "";
    private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private static final String CRLF = "\r\n";

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setBody(String body) {
        this.body = body != null ? body : "";
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.headers.putAll(headers);
    }

    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public void setContentTypeFromFilename(String filename) {
        String mimeType = MimeTypeDetector.detectMimeType(filename);
        setHeader("Content-Type", mimeType);
    }

    private static final Map<Integer, String> REASON_PHRASES = Map.ofEntries(
            Map.entry(200, "OK"),
            Map.entry(201, "Created"),
            Map.entry(204, "No Content"),
            Map.entry(301, "Moved Permanently"),
            Map.entry(302, "Found"),
            Map.entry(303, "See Other"),
            Map.entry(304, "Not Modified"),
            Map.entry(307, "Temporary Redirect"),
            Map.entry(308, "Permanent Redirect"),
            Map.entry(400, "Bad Request"),
            Map.entry(401, "Unauthorized"),
            Map.entry(403, "Forbidden"),
            Map.entry(404, "Not Found"),
            Map.entry(500, "Internal Server Error"),
            Map.entry(502, "Bad Gateway"),
            Map.entry(503, "Service Unavailable")
    );

    public String build(){
        StringBuilder sb = new StringBuilder();
        String reason = REASON_PHRASES.getOrDefault(statusCode, "");

        sb.append(PROTOCOL).append(" ").append(statusCode);
        if (!reason.isEmpty()) {
            sb.append(" ").append(reason);
        }
        sb.append(CRLF);

        // User-defined headers
        headers.forEach((k,v) -> sb.append(k).append(": ").append(v).append(CRLF));

        // Auto-append Content-Length if not set
        if (!headers.containsKey("Content-Length")) {
            sb.append("Content-Length: ")
                    .append(body.getBytes(StandardCharsets.UTF_8).length)
                    .append(CRLF);
        }

        // Auto-append Connection if not set
        if (!headers.containsKey("Connection")) {
            sb.append("Connection: close").append(CRLF);
        }

        // Blank line before body
        sb.append(CRLF);

        // Body
        sb.append(body);

        return sb.toString();
    }
}