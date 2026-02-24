package org.example.http;

import org.example.filter.FilterChain;
import org.example.httpparser.HttpRequest;
//
class TestFilterChain implements FilterChain {
    boolean called = false;

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response) {
        called = true;
    }
}