package org.example.filter;

import org.example.httpparser.HttpRequest;
import org.example.http.HttpResponseBuilder;

import java.util.List;
import java.util.function.BiConsumer;

public class FilterChainImpl implements FilterChain {

    private final List<Filter> filters;
    private final BiConsumer<HttpRequest, HttpResponseBuilder> terminalHandler;
    private int index = 0;

    public FilterChainImpl(List<Filter> filters) {
        this(filters, (req, resp) -> {
            // default no-op (preserves previous behavior)
        });
    }

    public FilterChainImpl(List<Filter> filters,
                           BiConsumer<HttpRequest, HttpResponseBuilder> terminalHandler) {
        this.filters = filters;
        this.terminalHandler = terminalHandler;
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response) {
        if (index < filters.size()) {
            Filter next = filters.get(index++);
            next.doFilter(request, response, this);
        } else {
            terminalHandler.accept(request, response);
        }
    }
}
