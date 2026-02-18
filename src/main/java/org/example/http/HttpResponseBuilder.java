package org.example.http;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class HttpResponseBuilder {

    private static final String PROTOCOL = "HTTP/1.1";
    private int statusCode = 200;
    private String body = "";
    private byte[] bytebody;
    private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private static final String CRLF = "\r\n";

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

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public void setBody(String body) {
        this.body = body != null ? body : "";
        this.bytebody = null; // Clear byte body when setting string body
    }

    public void setBody(byte[] body) {
        this.bytebody = body;
        this.body = ""; // Clear string body when setting byte body
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

    /*
     * Builds the complete HTTP response as a byte array and preserves binary content without corruption.
     * @return Complete HTTP response (headers + body) as byte[]
     */
    public byte[] build() {
        // Determine content body and length
        byte[] contentBody;
        int contentLength;

        if (bytebody != null) {
            contentBody = bytebody;
            contentLength = bytebody.length;
        } else {
            contentBody = body.getBytes(StandardCharsets.UTF_8);
            contentLength = contentBody.length;
        }

        // Build headers as String
        StringBuilder headerBuilder = new StringBuilder();

        // Status line
        String reason = REASON_PHRASES.getOrDefault(statusCode, "");
        headerBuilder.append(PROTOCOL).append(" ").append(statusCode);
        if (!reason.isEmpty()) {
            headerBuilder.append(" ").append(reason);
        }
        headerBuilder.append(CRLF);

        // User-defined headers
        headers.forEach((k, v) -> headerBuilder.append(k).append(": ").append(v).append(CRLF));

        // Auto-append Content-Length if not set.
        if (!headers.containsKey("Content-Length")) {
            headerBuilder.append("Content-Length: ").append(contentLength).append(CRLF);
        }

        // Auto-append Connection if not set.
        if (!headers.containsKey("Connection")) {
            headerBuilder.append("Connection: close").append(CRLF);
        }

        // Blank line before body
        headerBuilder.append(CRLF);

        // Convert headers to bytes
        byte[] headerBytes = headerBuilder.toString().getBytes(StandardCharsets.UTF_8);

        // Combine headers + body into single byte array
        byte[] response = new byte[headerBytes.length + contentBody.length];
        System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
        System.arraycopy(contentBody, 0, response, headerBytes.length, contentBody.length);

        return response;
    }
}