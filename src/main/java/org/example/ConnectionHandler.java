package org.example;

import org.example.config.AppConfig;
import org.example.filter.IpFilter;
import org.example.httpparser.HttpParser;
import org.example.httpparser.HttpRequest;

import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements AutoCloseable {

    Socket client;
    String uri;

    public ConnectionHandler(Socket client) {
        this.client = client;
    }

    public void runConnectionHandler() throws IOException {
        HttpParser parser = new HttpParser();
        parser.setReader(client.getInputStream());
        parser.parseRequest();
        parser.parseHttp();

        // Create HttpRequest object
        HttpRequest request = new HttpRequest(
                parser.getMethod(),
                parser.getUri(),
                parser.getVersion(),
                parser.getHeadersMap(),
                ""
        );

        // Set client IP address
        String clientIp = client.getInetAddress().getHostAddress();
        request.setAttribute("clientIp", clientIp);

        // Check if IP filter is enabled
        AppConfig config = org.example.config.ConfigLoader.get();
        AppConfig.IpFilterConfig ipFilterConfig = config.ipFilter();

        if (Boolean.TRUE.equals(ipFilterConfig.enabled())) {
            // Create and run IP filter
            IpFilter ipFilter = createIpFilterFromConfig(ipFilterConfig);
            org.example.http.HttpResponseBuilder response = new org.example.http.HttpResponseBuilder();

            // Create a simple filter chain with just the IP filter
            ipFilter.doFilter(request, response, (req, resp) -> {
                // This lambda is called if IP is allowed
                // We don't do anything here, just continue
            });

            // Check if the response has an error status (blocked)
            // If response body is set, it means the IP was blocked
            byte[] responseBytes = response.build();
            String responseStr = new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);

            if (responseStr.contains("403 Forbidden") || responseStr.contains("400 Bad Request")) {
                // IP was blocked - send error response and return
                client.getOutputStream().write(responseBytes);
                client.getOutputStream().flush();
                return;
            }
        }

        // IP is allowed (or filter disabled) - continue with normal file serving
        resolveTargetFile(parser.getUri());
        StaticFileHandler sfh = new StaticFileHandler();
        sfh.sendGetRequest(client.getOutputStream(), uri);
    }

    private void resolveTargetFile(String uri) {
        if (uri.matches("/$")) { //matches(/)
            this.uri = "index.html";
        } else if (uri.matches("^(?!.*\\.html$).*$")) {
            this.uri = uri.concat(".html");
        } else {
            this.uri = uri;
        }

    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    private IpFilter createIpFilterFromConfig(AppConfig.IpFilterConfig config) {
        IpFilter filter = new IpFilter();

        // Set mode
        if ("ALLOWLIST".equalsIgnoreCase(config.mode())) {
            filter.setMode(IpFilter.FilterMode.ALLOWLIST);
        } else {
            filter.setMode(IpFilter.FilterMode.BLOCKLIST);
        }

        // Add blocked IPs
        for (String ip : config.blockedIps()) {
            filter.addBlockedIp(ip);
        }

        // Add allowed IPs
        for (String ip : config.allowedIps()) {
            filter.addAllowedIp(ip);
        }

        filter.init();
        return filter;
    }
}
