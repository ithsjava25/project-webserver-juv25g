package org.example.server;

import java.util.Objects;

public final class FilterChain {
    @FunctionalInterface
    public interface TerminalHandler {
        void handle(HttpRequest request, HttpResponse response);
    }

    private final HttpFilter[] filters;
    private final TerminalHandler terminal;
    private int index = 0;

    public FilterChain(HttpFilter[] filters, TerminalHandler terminal) {
        this.filters = Objects.requireNonNull(filters, "filters");
        this.terminal = Objects.requireNonNull(terminal, "terminal");
    }

    public void doFilter(HttpRequest request, HttpResponse response) {
        if (index < filters.length) {
            HttpFilter current = filters[index++];
            current.doFilter(request, response, this);
            return;
        }
        terminal.handle(request, response);
    }
}