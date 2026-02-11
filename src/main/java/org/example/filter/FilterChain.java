package org.example.filter;

import org.example.http.HttpResponseBuilder;

import java.net.http.HttpRequest;

public interface FilterChain {

    void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain);
}
