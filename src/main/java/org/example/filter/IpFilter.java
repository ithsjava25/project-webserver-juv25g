package org.example.filter;


import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.util.HashSet;
import java.util.Set;

public class IpFilter implements Filter {

    private final Set<String> blockedIps = new HashSet<>();
    private final Set<String> allowedIps = new HashSet<>();
    private FilterMode mode = FilterMode.BLOCKLIST;

    public enum FilterMode {
        ALLOWLIST,
        BLOCKLIST
    }

    @Override
    public void init() {}

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
        String clientIp = (String) request.getAttribute("clientIp");

        if (clientIp == null) {
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
    public void destroy() {}

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
