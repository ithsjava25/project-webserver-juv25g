package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
/// /
class TestFilterChain implements FilterChain {
    boolean called = false;

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response) {
        called = true;
    }
}