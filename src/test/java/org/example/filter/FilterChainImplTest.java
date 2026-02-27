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
        //Arrange
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

    @Test
    void calls_filters_then_terminal_handler_in_order() {
        //Arrange

        // We create mocks for our filters and our TerminalHandler.
        // This allows us to track exactly when and in what order they are called.
        Filter filter1 = mock(Filter.class);
        Filter filter2 = mock(Filter.class);
        TerminalHandler handler = mock(TerminalHandler.class);

        /* * IMPORTANT: In a Chain of Responsibility, a filter must manually call chain.doFilter()
         * to pass on to the next filter in line. If we don't "teach" our mocks to do this,
         * the chain will stop dead at the first filter.
         */
        doAnswer(invocation -> {
            // Here we simulate Filter 1 performing its check and then forwarding the request.
            HttpRequest req = invocation.getArgument(0);
            HttpResponseBuilder resp = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            chain.doFilter(req, resp);
            return null;
        }).when(filter1).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            // Filter 2 receives the request and forwards it again.
            HttpRequest req = invocation.getArgument(0);
            HttpResponseBuilder resp = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            chain.doFilter(req, resp);
            return null;
        }).when(filter2).doFilter(any(), any(), any());

        // Create the actual chain, injecting our filters and setting the final "station" TerminalHandler.
        FilterChainImpl chain = new FilterChainImpl(List.of(filter1, filter2), handler);

        HttpRequest request = new HttpRequest("GET", "index.html", "HTTP/1.1", Map.of(), "");
        HttpResponseBuilder response = new HttpResponseBuilder();

        //Act
        chain.doFilter(request, response);

        //Assert

        // We use inOrder to verify that the execution happened in the exact sequence:
        // Filter 1 -> Filter 2 -> TerminalHandler
        var inOrder = inOrder(filter1, filter2, handler);

        // Verify that the filters are being called in order and that the TerminalHandler is reached
        inOrder.verify(filter1, times(1)).doFilter(any(), any(), any());
        inOrder.verify(filter2, times(1)).doFilter(any(), any(), any());
        inOrder.verify(handler, times(1)).handle(any(), any());

        inOrder.verifyNoMoreInteractions();
    }

}