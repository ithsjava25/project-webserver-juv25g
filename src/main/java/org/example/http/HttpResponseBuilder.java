package org.example.http;
//
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponseBuilder {

    private static final String protocol = "HTTP/1.1";
    private int statusCode = 200;
    private String statusMessage = "OK";
    private String body = "";
    private Map<String, String> headers = new LinkedHashMap<>();

    private static final String CRLF = "\r\n";


    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setBody(String body) {
        this.body = body;
    }
    public void  setHeaders(Map<String, String> headers) {
        this.headers = headers;;
    }

    public String build(){
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append(" ").append(statusCode).append(" ").append(statusMessage).append(CRLF);
        headers.forEach((k,v) -> sb.append(k).append(": ").append(v).append(CRLF));
        sb.append("Content-Length: ")
                .append(body.getBytes(StandardCharsets.UTF_8).length);
        sb.append(CRLF);
        sb.append("Connection: close").append(CRLF);
        sb.append(CRLF);
        sb.append(body);
        return sb.toString();

    }

}
