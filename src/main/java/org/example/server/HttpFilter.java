package org.example.server;

@FunctionalInterface
public interface HttpFilter {
    void doFilter(HttpRequest request, HttpResponse response, FilterChain chain);
}
