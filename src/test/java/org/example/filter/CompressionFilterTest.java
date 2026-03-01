package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

class CompressionFilterTest {

    @Test
    void testGzipCompressionWhenClientSupportsIt() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip, deflate");

        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                headers,
                null
        );

        String largeBody = "<html><body>" + "Hello World! ".repeat(200) + "</body></html>";
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setBody(largeBody);
        response.setHeaders(Map.of("Content-Type", "text/html"));

        FilterChain mockChain = (req, res) -> {
        };

        CompressionFilter filter = new CompressionFilter();
        filter.init();
        filter.doFilter(request, response, mockChain);

        byte[] compressedBody = getBodyFromResponse(response);
        assertNotNull(compressedBody, "Body should not be null");
        assertTrue(compressedBody.length < largeBody.getBytes(StandardCharsets.UTF_8).length,
                "Compressed body should be smaller than original");


        String decompressed = decompressGzip(compressedBody);
        assertEquals(largeBody, decompressed, "Decompressed data should match original");
    }

    @Test
    void testNoCompressionWhenClientDoesNotSupport() {
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Map.of(),
                null
        );

        String body = "<html><body>" + "Hello World! ".repeat(200) + "</body></html>";
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setBody(body);

        FilterChain mockChain = (req, res) -> {};

        CompressionFilter filter = new CompressionFilter();
        filter.doFilter(request, response, mockChain);

        byte[] resultBody = getBodyFromResponse(response);
        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), resultBody,
                "Body should not be compressed when client doesn't support gzip");
    }

    @Test
    void testNoCompressionForSmallResponses() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip");

        HttpRequest request = new HttpRequest("GET", "/", "HTTP/1.1", headers, null);

        String smallBody = "Hello";
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setBody(smallBody);

        FilterChain mockChain = (req, res) -> {};

        CompressionFilter filter = new CompressionFilter();
        filter.doFilter(request, response, mockChain);

        byte[] resultBody = getBodyFromResponse(response);
        assertArrayEquals(smallBody.getBytes(StandardCharsets.UTF_8), resultBody,
                "Small bodies should not be compressed");
    }

    private String decompressGzip(byte[] compressed) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (GZIPInputStream gzis = new GZIPInputStream(bais)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        }

        return baos.toString(StandardCharsets.UTF_8);
    }

    private byte[] getBodyFromResponse(HttpResponseBuilder response) {
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
            return body.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get body", e);
        }
    }
    @Test
    void testSkipCompressionForImages() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip");

        HttpRequest request = new HttpRequest("GET", "/image.jpg", "HTTP/1.1", headers, null);

        String largeImageData = "fake image data ".repeat(200);
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setBody(largeImageData);
        response.setHeaders(Map.of("Content-Type", "image/jpeg"));

        FilterChain mockChain = (req, res) -> {};

        CompressionFilter filter = new CompressionFilter();
        filter.doFilter(request, response, mockChain);

        byte[] resultBody = getBodyFromResponse(response);
        assertArrayEquals(largeImageData.getBytes(StandardCharsets.UTF_8), resultBody,
                "Images should not be compressed");
    }

    @Test
    void testCompressJsonResponse() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip");

        HttpRequest request = new HttpRequest("GET", "/api/data", "HTTP/1.1", headers, null);

        String jsonData = "{\"data\": " + "\"value\",".repeat(200) + "}";
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setBody(jsonData);
        response.setHeaders(Map.of("Content-Type", "application/json"));

        FilterChain mockChain = (req, res) -> {};

        CompressionFilter filter = new CompressionFilter();
        filter.doFilter(request, response, mockChain);

        byte[] resultBody = getBodyFromResponse(response);
        assertTrue(resultBody.length < jsonData.getBytes(StandardCharsets.UTF_8).length,
                "JSON should be compressed");
    }

    @Test
    void testHandleContentTypeWithCharset() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip");

        HttpRequest request = new HttpRequest("GET", "/", "HTTP/1.1", headers, null);

        String body = "<html>" + "content ".repeat(200) + "</html>";
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setBody(body);
        response.setHeaders(Map.of("Content-Type", "text/html; charset=UTF-8"));

        FilterChain mockChain = (req, res) -> {};

        CompressionFilter filter = new CompressionFilter();
        filter.doFilter(request, response, mockChain);

        byte[] resultBody = getBodyFromResponse(response);
        assertTrue(resultBody.length < body.getBytes(StandardCharsets.UTF_8).length,
                "Should compress even when Content-Type has charset");
    }
}