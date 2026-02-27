package org.example.filter;


import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A filter that rejects requests with bodies larger than a configured byte limit.
 * It primarily uses the Content-Length header (when present), and can also validate
 * a parsed request body if available.
 */
public class MaxRequestBodySizeFilter implements Filter {

    private final long maxBytes;

    public MaxRequestBodySizeFilter(long maxBytes) {
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxBytes must be >= 0");
        }
        this.maxBytes = maxBytes;
    }
    @Override
    public void init() {

    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
        
        if (!mayHaveBody(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        Long contentLength = getHeaderAsLong(request.getHeaders(), "Content-Length");

        if (contentLength != null && contentLength > maxBytes) {
            reject(response, contentLength);
            return;
        }

        // fallback: if a body has already been parsed, validate it as well
        String body = request.getBody();
        if (body != null && !body.isEmpty()) {
            int bodyBytes = body.getBytes(StandardCharsets.UTF_8).length;
            if (bodyBytes > maxBytes) {
                reject(response, (long) bodyBytes);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    private boolean mayHaveBody(String method) {
        if (method == null) {
            return false;
        }
        String m = method.trim().toUpperCase();
        return m.equals("POST") || m.equals("PUT") || m.equals("PATCH");
    }

    private Long getHeaderAsLong(Map<String, String> headers, String s) {
    }

    private void reject(HttpResponseBuilder response, Long contentLength) {
    }
}
