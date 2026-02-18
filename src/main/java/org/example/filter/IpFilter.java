package org.example.filter;


import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.util.HashSet;
import java.util.Set;

/**
 * A filter that allows or blocks HTTP requests based on the client's IP address.
 * The filter supports two modes:
 * ALLOWLIST – only IP addresses in the allowlist are permitted
 * LOCKLIST – all IP addresses are permitted except those in the blocklist
 */
public class IpFilter implements Filter {

    private final Set<String> blockedIps = new HashSet<>();
    private final Set<String> allowedIps = new HashSet<>();
    private FilterMode mode = FilterMode.BLOCKLIST;

    /**
     * Defines the filtering mode.
     */
    public enum FilterMode {
        ALLOWLIST,
        BLOCKLIST
    }

    @Override
    public void init() {
        // Intentionally empty - no initialization needed
    }

    /**
     * Filters incoming HTTP requests based on the client's IP address.
     *
     * @param request  the incoming HTTP request
     * @param response the HTTP response builder used when blocking requests
     * @param chain    the filter chain to continue if the request is allowed
     */
    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
        String clientIp = (String) request.getAttribute("clientIp");

        if (clientIp == null || clientIp.trim().isEmpty()) {
            response.setStatusCode(400);
            response.setBody("Bad Request: Missing client IP address");
            return;
        }

        boolean allowed = isIpAllowed(clientIp);

        if (allowed) {
            chain.doFilter(request, response);
        } else {
            response.setStatusCode(403);
            response.setBody("Forbidden: IP address " + clientIp + " is not allowed");
        }
    }

    @Override
    public void destroy() {
        // Intentionally empty - no cleanup needed
    }

    /**
     * Determines whether an IP address is allowed based on the current filter mode.
     *
     * @param ip the IP address to check
     * @return true if the IP address is allowed, otherwise false
     */
    private boolean isIpAllowed(String ip) {
        if (mode == FilterMode.ALLOWLIST) {
            return allowedIps.contains(ip);
        } else {
            return !blockedIps.contains(ip);
        }
    }

    public void setMode(FilterMode mode) {
        this.mode = mode;
    }

    public void addBlockedIp(String ip) {
        blockedIps.add(ip);
    }

    public void addAllowedIp(String ip) {
        allowedIps.add(ip);
    }
}
