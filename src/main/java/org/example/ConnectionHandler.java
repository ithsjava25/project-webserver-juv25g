package org.example;

import org.example.config.AppConfig;
import org.example.filter.IpFilter;
import org.example.httpparser.HttpParser;
import org.example.httpparser.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import org.example.filter.Filter;
import org.example.filter.FilterChainImpl;
import org.example.http.HttpResponseBuilder;
import org.example.config.ConfigLoader;

import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements AutoCloseable {

    Socket client;
    String uri;
    private final List<Filter> filters;
    String webRoot;

    public ConnectionHandler(Socket client) {
        this.client = client;
        this.filters = buildFilters();
        this.webRoot = null;
    }

    public ConnectionHandler(Socket client, String webRoot) {
        this.client = client;
        this.webRoot = webRoot;
        this.filters = buildFilters();
    }

    private List<Filter> buildFilters() {
        List<Filter> list = new ArrayList<>();
        AppConfig config = ConfigLoader.get();
        AppConfig.IpFilterConfig ipFilterConfig = config.ipFilter();
        if (Boolean.TRUE.equals(ipFilterConfig.enabled())) {
            list.add(createIpFilterFromConfig(ipFilterConfig));
        }
        // Add more filters here...
        return list;
    }

    public void runConnectionHandler() throws IOException {
        StaticFileHandler sfh;

        if (webRoot != null) {
            sfh = new StaticFileHandler(webRoot);
        } else {
            sfh = new StaticFileHandler();
        }

        HttpParser parser = new HttpParser();
        parser.setReader(client.getInputStream());
        parser.parseRequest();
        parser.parseHttp();

        HttpRequest request = new HttpRequest(
                parser.getMethod(),
                parser.getUri(),
                parser.getVersion(),
                parser.getHeadersMap(),
                ""
        );

        String clientIp = client.getInetAddress().getHostAddress();
        request.setAttribute("clientIp", clientIp);

        HttpResponseBuilder response = applyFilters(request);

        int statusCode = response.getStatusCode();
        if (statusCode == HttpResponseBuilder.SC_FORBIDDEN ||
                statusCode == HttpResponseBuilder.SC_BAD_REQUEST) {
            byte[] responseBytes = response.build();
            client.getOutputStream().write(responseBytes);
            client.getOutputStream().flush();
            return;
        }

        resolveTargetFile(parser.getUri());
        sfh.sendGetRequest(client.getOutputStream(), uri);
    }

    private HttpResponseBuilder applyFilters(HttpRequest request) {
        HttpResponseBuilder response = new HttpResponseBuilder();

        FilterChainImpl chain = new FilterChainImpl(filters);
        chain.doFilter(request, response);

        return response;
    }

    private void resolveTargetFile(String uri) {
        if (uri == null || "/".equals(uri)) {
            this.uri = "index.html";
        } else {
            this.uri = uri.startsWith("/") ? uri.substring(1) : uri;
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