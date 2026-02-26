package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.example.server.TerminalHandler;

import java.util.List;
import java.util.Objects;

public class FilterChainImpl implements FilterChain {

    private final List<Filter> filters;
    private final TerminalHandler terminalHandler;
    private int index = 0;

    public FilterChainImpl(List<Filter> filters) {
        this(filters, (req, resp) -> {
            // default no-op (preserves previous behavior)
        });
    }

    public FilterChainImpl(List<Filter> filters,
                           TerminalHandler terminalHandler) {
        this.filters = Objects.requireNonNull(filters, "filters must not be null");
        this.terminalHandler = Objects.requireNonNull(terminalHandler, "terminalHandler must not be null");
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response) {
        if (index < filters.size()) {
            Filter next = filters.get(index++);
            next.doFilter(request, response, this);
        } else {
            terminalHandler.handle(request, response);
        }
    }
}
