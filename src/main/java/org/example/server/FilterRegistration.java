package org.example.server;

import org.example.filter.Filter;

import java.util.List;
import java.util.Objects;

public record FilterRegistration(
        Filter filter,
        int order,
        List<String> routePatterns
) {
    public FilterRegistration {
        filter = Objects.requireNonNull(filter, "filter must not be null");
        routePatterns = routePatterns == null ? null : List.copyOf(routePatterns);
    }

    public boolean isGlobal() {
        return routePatterns == null || routePatterns.isEmpty();
    }
}
