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
    public void init() {

    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
        String clientIp = (String) request.getAttribute("clientIp");

        // If IP is not blocked, continue in the chain
        if (!blockedIps.contains(clientIp)) {
            chain.doFilter(request, response);
        } else {
            // if IP is blocked - set the status code to 403
            response.setStatusCode(403);
            response.setBody("Forbidden: IP address " + clientIp + " is not allowed");
        }
    }

    @Override
    public void destroy() {

    }

    public void setMode(FilterMode mode) {
        this.mode = mode;
    }

    public void addBlockedIp(String ip) {
        blockedIps.add(ip);
    }

    public void addAllowedIp(String ip) { allowedIps.add(ip); }
}
