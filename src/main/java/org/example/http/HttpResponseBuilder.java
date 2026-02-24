package org.example.http;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class HttpResponseBuilder {

    // SUCCESS
    public static final int SC_OK = 200;
    public static final int SC_CREATED = 201;
    public static final int SC_NO_CONTENT = 204;

    // REDIRECTION
    public static final int SC_MOVED_PERMANENTLY = 301;
    public static final int SC_FOUND = 302;
    public static final int SC_SEE_OTHER = 303;
    public static final int SC_NOT_MODIFIED = 304;
    public static final int SC_TEMPORARY_REDIRECT = 307;
    public static final int SC_PERMANENT_REDIRECT = 308;

    // CLIENT ERROR
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_NOT_FOUND = 404;

    // SERVER ERROR
    public static final int SC_INTERNAL_SERVER_ERROR = 500;
    public static final int SC_BAD_GATEWAY = 502;
    public static final int SC_SERVICE_UNAVAILABLE = 503;
    public static final int SC_GATEWAY_TIMEOUT = 504;



    private static final String PROTOCOL = "HTTP/1.1";
    private int statusCode = SC_OK;
    private String body = "";
    private byte[] bytebody;
    private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private static final String CRLF = "\r\n";

    private static final Map<Integer, String> REASON_PHRASES = Map.ofEntries(
            Map.entry(SC_OK, "OK"),
            Map.entry(SC_CREATED, "Created"),
            Map.entry(SC_NO_CONTENT, "No Content"),
            Map.entry(SC_MOVED_PERMANENTLY, "Moved Permanently"),
            Map.entry(SC_FOUND, "Found"),
            Map.entry(SC_SEE_OTHER, "See Other"),
            Map.entry(SC_NOT_MODIFIED, "Not Modified"),
            Map.entry(SC_TEMPORARY_REDIRECT, "Temporary Redirect"),
            Map.entry(SC_PERMANENT_REDIRECT, "Permanent Redirect"),
            Map.entry(SC_BAD_REQUEST, "Bad Request"),
            Map.entry(SC_UNAUTHORIZED, "Unauthorized"),
            Map.entry(SC_FORBIDDEN, "Forbidden"),
            Map.entry(SC_NOT_FOUND, "Not Found"),
            Map.entry(SC_INTERNAL_SERVER_ERROR, "Internal Server Error"),
            Map.entry(SC_BAD_GATEWAY, "Bad Gateway"),
            Map.entry(SC_SERVICE_UNAVAILABLE, "Service Unavailable"),
            Map.entry(SC_GATEWAY_TIMEOUT, "Gateway Timeout")
    );

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setBody(String body) {
        this.body = body != null ? body : "";
        this.bytebody = null;
    }

    public void setBody(byte[] body) {
        this.bytebody = body;
        this.body = "";
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
        byte[] contentBody;
        int contentLength;

        if (bytebody != null) {
            contentBody = bytebody;
            contentLength = bytebody.length;
        } else {
            contentBody = body.getBytes(StandardCharsets.UTF_8);
            contentLength = contentBody.length;
        }

        StringBuilder headerBuilder = new StringBuilder();

        String reason = REASON_PHRASES.getOrDefault(statusCode, "");
        headerBuilder.append(PROTOCOL).append(" ").append(statusCode);
        if (!reason.isEmpty()) {
            headerBuilder.append(" ").append(reason);
        }
        headerBuilder.append(CRLF);

        headers.forEach((k, v) -> headerBuilder.append(k).append(": ").append(v).append(CRLF));

        if (!headers.containsKey("Content-Length")) {
            headerBuilder.append("Content-Length: ").append(contentLength).append(CRLF);
        }

        if (!headers.containsKey("Connection")) {
            headerBuilder.append("Connection: close").append(CRLF);
        }

        headerBuilder.append(CRLF);

        byte[] headerBytes = headerBuilder.toString().getBytes(StandardCharsets.UTF_8);

        byte[] response = new byte[headerBytes.length + contentBody.length];
        System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
        System.arraycopy(contentBody, 0, response, headerBytes.length, contentBody.length);

        return response;
    }
}
