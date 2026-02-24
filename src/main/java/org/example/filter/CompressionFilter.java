package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Compression filter that compresses HTTP responses with gzip when supported by client.
 *
 * <p>This filter applies gzip compression to HTTP responses when the following conditions are met:
 * <ul>
 *   <li>Client sends Accept-Encoding header containing "gzip"</li>
 *   <li>Response body is larger than 1KB (MIN_COMPRESS_SIZE)</li>
 *   <li>Content-Type is compressible (text-based formats like HTML, CSS, JS, JSON)</li>
 *   <li>Content-Type is not already compressed (images, videos, zip files)</li>
 * </ul>
 *
 * <p>When compression is applied, the filter:
 * <ul>
 *   <li>Compresses the response body using gzip</li>
 *   <li>Sets Content-Encoding: gzip header</li>
 *   <li>Sets Vary: Accept-Encoding header for proper caching</li>
 * </ul>
 *
 */

public class CompressionFilter implements Filter {
    private static final int MIN_COMPRESS_SIZE = 1024;

    private static final Set<String> COMPRESSIBLE_TYPES = Set.of(
            "text/html",
            "text/css",
            "text/javascript",
            "application/javascript",
            "application/json",
            "application/xml",
            "text/plain"
    );

    private static final Set<String> SKIP_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "image/webp", "video/mp4", "application/zip",
            "application/gzip", "application/x-gzip"
    );


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
        if (response.getHeader("Content-Encoding") != null) {
            return;
        }

        String acceptEncoding = getHeader(request, "Accept-Encoding");
        if (acceptEncoding == null || !acceptEncoding.toLowerCase().contains("gzip")) {
            return;
        }

        byte[] originalBody = response.getBodyBytes();
        if (originalBody == null || originalBody.length < MIN_COMPRESS_SIZE) {
            return;
        }

        String contentType = response.getHeader("Content-Type");
        if (!shouldCompress(contentType)) {
            return;
        }

        try {
            byte[] compressed = gzipCompress(originalBody);

            response.setBody(compressed);
            response.setHeader("Content-Encoding", "gzip");

            String existingVary = response.getHeader("Vary");
            if (existingVary != null && !existingVary.isEmpty()) {
                if (!existingVary.toLowerCase().contains("accept-encoding")) {
                    response.setHeader("Vary", existingVary + ", Accept-Encoding");
                }
            } else {
                response.setHeader("Vary", "Accept-Encoding");
            }

        } catch (IOException e) {
            System.err.println("CompressionFilter: gzip compression failed: " + e.getMessage());
        }
    }

    private boolean shouldCompress(String contentType) {
        if (contentType == null) {
            return false;
        }

        String baseType = contentType.split(";")[0].trim().toLowerCase();

        if (SKIP_TYPES.contains(baseType)) {
            return false;
        }

        return COMPRESSIBLE_TYPES.contains(baseType) ||
                baseType.startsWith("text/");
    }

    private String getHeader(HttpRequest request, String headerName) {
        Map<String, String> headers = request.getHeaders();

        String value = headers.get(headerName);
        if (value != null) return value;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(headerName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private byte[] gzipCompress(byte[] data) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);

        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(data);
        }

        return byteStream.toByteArray();
    }

    @Override
    public void destroy() {
    }
}