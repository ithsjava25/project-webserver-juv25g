package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

public class CompressionFilter implements Filter {

    @Override
    public void init() {
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response,
                         FilterChain chain) {
        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {
    }
}