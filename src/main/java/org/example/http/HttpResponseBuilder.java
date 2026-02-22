package org.example.http;
//
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponseBuilder {

    private static final String PROTOCOL = "HTTP/1.1";
    private int statusCode = 200;
    private String body = "";
    private byte[] bytebody;
    private Map<String, String> headers = new LinkedHashMap<>();
    private long contentLength = 0;

    private static final String CRLF = "\r\n";

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setBody(String body) {
        this.body = body != null ? body : "";
    }

    public void setBody(byte[] body) {
        this.bytebody = body;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = new LinkedHashMap<>(headers);
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    private static final Map<Integer, String> REASON_PHRASES = Map.of(
            200, "OK",
            201, "Created",
            400, "Bad Request",
            404, "Not Found",
            500, "Internal Server Error");

    // Ny metod: skicka endast headers (för streaming)
    public String buildHeaders() {
        StringBuilder sb = new StringBuilder();
        
        String reason = REASON_PHRASES.getOrDefault(statusCode, "OK");
        sb.append(PROTOCOL).append(" ").append(statusCode).append(" ").append(reason).append(CRLF);
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append(CRLF));
        sb.append("Content-Length: ").append(contentLength).append(CRLF);
        sb.append("Connection: close").append(CRLF);
        sb.append(CRLF);
        
        return sb.toString();
    }

    // Befintlig metod för bakåtkompatibilitet
    public String build() {
        StringBuilder sb = new StringBuilder();
        int contentLength;
        if (body.isEmpty() && bytebody != null) {
            contentLength = bytebody.length;
            setBody(new String(bytebody, StandardCharsets.UTF_8));
        } else {
            contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        }

        String reason = REASON_PHRASES.getOrDefault(statusCode, "OK");
        sb.append(PROTOCOL).append(" ").append(statusCode).append(" ").append(reason).append(CRLF);
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append(CRLF));
        sb.append("Content-Length: ").append(contentLength).append(CRLF);
        sb.append("Connection: close").append(CRLF);
        sb.append(CRLF);
        sb.append(body);
        return sb.toString();
    }
}
