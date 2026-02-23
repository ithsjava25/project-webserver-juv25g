package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.util.Map;

public class CompressionFilter implements Filter {

    @Override
    public void init() {
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response,
                         FilterChain chain) {
        chain.doFilter(request, response);

        compressIfNeeded(request, response);
    }
    private void compressIfNeeded(HttpRequest request, HttpResponseBuilder response) {
        String acceptEncoding = getHeader(request, "Accept-Encoding");
        if (acceptEncoding == null || !acceptEncoding.toLowerCase().contains("gzip")) {
            return; // Klienten stödjer inte gzip
        }

        System.out.println("Client accepts gzip compression"); // Debug
    }
    private String getHeader(HttpRequest request, String headerName) {
        Map<String, String> headers = request.getHeaders();

        // Exakt match först
        String value = headers.get(headerName);
        if (value != null) return value;

        // Case-insensitive fallback
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(headerName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void destroy() {
    }
}