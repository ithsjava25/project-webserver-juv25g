package org.example.server;

import org.example.filter.Filter;
import org.example.filter.FilterChainImpl;
import org.example.httpparser.HttpRequest;
import org.example.http.HttpResponseBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ConfigurableFilterPipeline {

    private final List<FilterRegistration> registrations;

    public ConfigurableFilterPipeline(List<FilterRegistration> registrations) {
        this.registrations = List.copyOf(
                Objects.requireNonNull(registrations, "registrations must not be null")
        );
    }

    public HttpResponseBuilder execute(HttpRequest request,
                                       BiConsumer<HttpRequest, HttpResponseBuilder> terminalHandler) {

        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(terminalHandler, "terminalHandler must not be null");

        List<FilterRegistration> globalRegs = new ArrayList<>();
        List<FilterRegistration> routeRegs = new ArrayList<>();

        for (FilterRegistration reg : registrations) {
            if (reg.isGlobal()) {
                globalRegs.add(reg);
            } else {
                if (matchesAny(reg.routePatterns(), request.getPath())) {
                    routeRegs.add(reg);
                }
            }
        }

        Comparator<FilterRegistration> byOrder =
                Comparator.comparingInt(FilterRegistration::order);

        globalRegs.sort(byOrder);
        routeRegs.sort(byOrder);

        List<Filter> allFilters = new ArrayList<>(globalRegs.size() + routeRegs.size());
        for (FilterRegistration reg : globalRegs) {
            allFilters.add(reg.filter());
        }
        for (FilterRegistration reg : routeRegs) {
            allFilters.add(reg.filter());
        }

        HttpResponseBuilder response = new HttpResponseBuilder();
        new FilterChainImpl(allFilters, terminalHandler).doFilter(request, response);
        return response;
    }

    private boolean matchesAny(List<String> patterns, String path) {
        if (patterns == null || path == null) return false;

        for (String pattern : patterns) {
            if (RoutePattern.matches(pattern, path)) {
                return true;
            }
        }

        return false;
    }
}