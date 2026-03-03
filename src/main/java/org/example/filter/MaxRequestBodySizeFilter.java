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
        if (maxBytes < 0) {
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
            int bodySizeInBytes = body.getBytes(StandardCharsets.UTF_8).length;
            if (bodySizeInBytes > maxBytes) {
                reject(response, (long) bodySizeInBytes);
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
        String normalizedMethod = method.trim().toUpperCase();
        return normalizedMethod.equals("POST") || normalizedMethod.equals("PUT") || normalizedMethod.equals("PATCH");
    }

    private Long getHeaderAsLong(Map<String, String> headers, String headerName) {
        if (headers == null || headerName == null) {
            return null;
        }
        String rawHeaderValue = null;
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            String currentHeaderName = headerEntry.getKey();
            if (currentHeaderName.equalsIgnoreCase(headerName)) {
                rawHeaderValue = headerEntry.getValue();
                break;
            }
        }
        if (rawHeaderValue == null) {
            return null;
        }
        try {
            long parsedContentLength = Long.parseLong(rawHeaderValue.trim());
            return parsedContentLength < 0 ? null : parsedContentLength;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void reject(HttpResponseBuilder response, Long contentLength) {
        response.setStatusCode(HttpResponseBuilder.SC_PAYLOAD_TOO_LARGE);
        response.setBody("Payload too large: " + contentLength + " bytes (max " + maxBytes + ")");
    }
}
