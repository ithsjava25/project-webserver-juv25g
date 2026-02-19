package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;


public interface FilterChain {

    void doFilter(HttpRequest request, HttpResponseBuilder response);
}
