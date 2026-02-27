package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.example.server.TerminalHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FilterChainImplTest {

    @Test
    void calls_terminal_handler_when_no_filters() {

        TerminalHandler handler = mock(TerminalHandler.class);
        FilterChainImpl chain = new FilterChainImpl(List.of(), handler);

        HttpRequest request = new HttpRequest("GET", "index.html", "HTTP/1.1", Map.of(), "");
        HttpResponseBuilder response = new HttpResponseBuilder();

        //Act
        chain.doFilter(request, response);

        //Assert
        verify(handler, times(1)).handle(any(HttpRequest.class), any(HttpResponseBuilder.class));
        verifyNoMoreInteractions(handler);
    }

}