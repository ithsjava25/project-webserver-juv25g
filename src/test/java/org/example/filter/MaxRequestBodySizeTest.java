package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class MaxRequestBodySizeFilterTest {

    private static FilterChain spyChain(AtomicBoolean called) {
        return (req, resp) -> called.set(true);
    }

    @Test
    void getRequest_shouldPassThrough() {
        MaxRequestBodySizeFilter filter = new MaxRequestBodySizeFilter(10);

        HttpRequest request = new HttpRequest("GET", "/", "HTTP/1.1", Map.of(), null);
        HttpResponseBuilder response = new HttpResponseBuilder();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, spyChain(chainCalled));

        assertTrue(chainCalled.get());
        assertEquals(HttpResponseBuilder.SC_OK, response.getStatusCode());
    }

    @Test
    void postWithTooLargeContentLength_shouldRejectWith413() {
        MaxRequestBodySizeFilter filter = new MaxRequestBodySizeFilter(10);

        HttpRequest request = new HttpRequest(
                "POST", "/upload", "HTTP/1.1",
                Map.of("Content-Length", "11"),
                null
        );
        HttpResponseBuilder response = new HttpResponseBuilder();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, spyChain(chainCalled));

        assertFalse(chainCalled.get());
        assertEquals(HttpResponseBuilder.SC_PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertTrue(response.getBody().startsWith("Payload too large:"), "body should explain rejection");
    }

    @Test
    void postWithContentLengthEqualToMax_shouldPassThrough() {
        MaxRequestBodySizeFilter filter = new MaxRequestBodySizeFilter(10);

        HttpRequest request = new HttpRequest(
                "POST", "/upload", "HTTP/1.1",
                Map.of("Content-Length", "10"),
                null
        );
        HttpResponseBuilder response = new HttpResponseBuilder();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, spyChain(chainCalled));

        assertTrue(chainCalled.get());
        assertEquals(HttpResponseBuilder.SC_OK, response.getStatusCode());
    }

    @Test
    void invalidContentLength_shouldBeIgnoredAndPassThrough() {
        MaxRequestBodySizeFilter filter = new MaxRequestBodySizeFilter(10);

        HttpRequest request = new HttpRequest(
                "POST", "/upload", "HTTP/1.1",
                Map.of("Content-Length", "abc"),
                null
        );
        HttpResponseBuilder response = new HttpResponseBuilder();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, spyChain(chainCalled));

        assertTrue(chainCalled.get());
        assertEquals(HttpResponseBuilder.SC_OK, response.getStatusCode());
    }

    @Test
    void contentLengthHeaderName_shouldBeCaseInsensitive() {
        MaxRequestBodySizeFilter filter = new MaxRequestBodySizeFilter(10);

        HttpRequest request = new HttpRequest(
                "POST", "/upload", "HTTP/1.1",
                Map.of("content-length", "11"), // lowercase key
                null
        );
        HttpResponseBuilder response = new HttpResponseBuilder();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, spyChain(chainCalled));

        assertFalse(chainCalled.get());
        assertEquals(HttpResponseBuilder.SC_PAYLOAD_TOO_LARGE, response.getStatusCode());
    }

    @Test
    void bodyFallback_shouldCountUtf8Bytes() {
        // "€" is 3 bytes in UTF-8
        MaxRequestBodySizeFilter filter = new MaxRequestBodySizeFilter(2);

        HttpRequest request = new HttpRequest(
                "POST", "/upload", "HTTP/1.1",
                Map.of(), // no Content-Length
                "€"
        );
        HttpResponseBuilder response = new HttpResponseBuilder();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, spyChain(chainCalled));

        assertFalse(chainCalled.get());
        assertEquals(HttpResponseBuilder.SC_PAYLOAD_TOO_LARGE, response.getStatusCode());
    }

    @Test
    void negativeMaxBytes_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new MaxRequestBodySizeFilter(-1));
    }
}
