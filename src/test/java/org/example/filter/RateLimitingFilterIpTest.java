package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.example.filter.RateLimitingFilter.resolveClientIp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterIpTest {

    @Mock HttpRequest request;
    @Mock HttpResponseBuilder response;

    @Test
    void shouldUseXForwarded_WhenPresent(){

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Forwarded-For", "203.0.113.195");

        when(request.getHeaders()).thenReturn(headers);
        when(request.getAttribute("clientIp")).thenReturn("127.0.0.1");

        String finalIp = resolveClientIp(request, response);

        assertEquals("203.0.113.195", finalIp);
    }

    @Test
    void shouldFallbackToAttribute_WhenXForwardedForIsNotPresent(){

        Map<String, String> headers = new HashMap<>();
        when(request.getHeaders()).thenReturn(headers);
        when(request.getAttribute("clientIp")).thenReturn("10.0.0.5");

        String finalIp = resolveClientIp(request, response);

        assertEquals("10.0.0.5", finalIp);
    }
}
