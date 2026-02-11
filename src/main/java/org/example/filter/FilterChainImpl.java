package org.example.filter;

import org.example.http.HttpResponseBuilder;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.List;

/*
* The default class of FilterChain,
* Contains a list of filters. For each of the filter, will execute the doFilter method.
*
 */

public class FilterChainImpl implements FilterChain {

    private final List<Filter> filters;
    private int index = 0;

    public FilterChainImpl(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
        if (index < filters.size()) {
            Filter next = filters.get(index++);
            next.doFilter(request, response, this);
        } else {
            // TODO: when no more filters, should execute the request
        }
    }
}
