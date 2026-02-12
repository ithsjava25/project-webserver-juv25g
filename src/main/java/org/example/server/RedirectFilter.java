package org.example.server;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class RedirectFilter implements HttpFilter {
    private static final Logger LOG = Logger.getLogger(RedirectFilter.class.getName());
    private final List<RedirectRule> rules;

    public RedirectFilter(List<RedirectRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
        String path = request.path();

        for (RedirectRule rule : rules) {
            if (rule.matches(path)) {
                LOG.info(() -> "Redirecting " + path + " -> " + rule.getTargetUrl() + " (" + rule.getStatusCode() + ")");
                response.setStatus(rule.getStatusCode());
                response.setHeader("Location", rule.getTargetUrl());
                return; // STOP pipeline
            }
        }

        chain.doFilter(request, response);
    }
}
