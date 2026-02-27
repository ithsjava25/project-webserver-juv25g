package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.util.Map;

public class LocaleStatsFilter implements Filter {

    @Override
    public void init() {
    }

    @Override
    public void doFilter(HttpRequest request,
                         HttpResponseBuilder response,
                         FilterChain chain) {}

    @Override
    public void destroy() {}

}
