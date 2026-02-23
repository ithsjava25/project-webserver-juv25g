package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class CompressionFilter implements Filter {
    private static final int MIN_COMPRESS_SIZE = 1024;

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
            return;
        }

        System.out.println("Client accepts gzip compression");

        byte[] originalBody = getResponseBody(response);
        if (originalBody == null || originalBody.length < MIN_COMPRESS_SIZE) {
            System.out.println("Body too small to compress: " +
                    (originalBody != null ? originalBody.length : 0) + " bytes");
            return;
        }

        try {
            byte[] compressed = gzipCompress(originalBody);
            System.out.println("Compressed " + originalBody.length +
                    " bytes to " + compressed.length + " bytes (" +
                    (100 - (compressed.length * 100 / originalBody.length)) + "% reduction)");

            response.setBody(compressed);

        } catch (IOException e) {
            System.err.println("Gzip compression failed: " + e.getMessage());
        }
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
    private byte[] getResponseBody(HttpResponseBuilder response) {
        try {
            var field = response.getClass().getDeclaredField("bytebody");
            field.setAccessible(true);
            byte[] bytebody = (byte[]) field.get(response);

            if (bytebody != null) {
                return bytebody;
            }

            var bodyField = response.getClass().getDeclaredField("body");
            bodyField.setAccessible(true);
            String body = (String) bodyField.get(response);

            if (body != null && !body.isEmpty()) {
                return body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }

            return null;

        } catch (Exception e) {
            System.err.println("Failed to get response body: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void destroy() {
    }
}