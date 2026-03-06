package org.example.filter.redirect;

import org.example.filter.Filter;
import org.example.filter.FilterChain;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class RedirectFilter implements Filter {
    private static final Logger LOG = Logger.getLogger(RedirectFilter.class.getName());
    private final List<RedirectRule> rules;

    public RedirectFilter(List<RedirectRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }

    @Override
    public void init() {
        // no-op
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
    String path = request.getPath();

    if (path == null) {
        chain.doFilter(request, response);
        return;
    }

    for (RedirectRule rule : rules) {
        if (rule.matches(path)) {   // <-- DENNA RADEN SKA FINNAS
            final String sanitizedPath = path.replaceAll("[\\r\\n]", "_");
            LOG.info(() -> "Redirecting " + sanitizedPath + " -> " 
                    + rule.getTargetUrl() + " (" + rule.getStatusCode() + ")");

            response.setStatusCode(rule.getStatusCode());
            response.setHeader("Location", rule.getTargetUrl());
            return; // STOP pipeline
        }
    }

    chain.doFilter(request, response);
}


    @Override
    public void destroy() {
        // no-op
    }
}
