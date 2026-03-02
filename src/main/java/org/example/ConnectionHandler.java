package org.example;

import org.example.config.AppConfig;
import org.example.filter.IpFilter;
import org.example.httpparser.HttpParser;
import org.example.httpparser.HttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.filter.Filter;
import org.example.filter.FilterChainImpl;
import org.example.http.HttpResponseBuilder;
import org.example.config.ConfigLoader;

import java.io.IOException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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

        // --- ISSUE FIX ---
        String rawUri = parser.getUri();
        String pathOnly = extractPath(rawUri);
        Map<String, List<String>> queryParams = parseQueryParams(rawUri);

        HttpRequest request = new HttpRequest(
                parser.getMethod(),
                pathOnly,
                parser.getVersion(),
                parser.getHeadersMap(),
                "",
                queryParams
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

        resolveTargetFile(request.getPath());
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

    // -----------------------------
    // Query parsing without split()
    // -----------------------------

    private static String extractPath(String uri) {
        if (uri == null || uri.isEmpty()) return "/";
        int q = uri.indexOf('?');
        String path = (q >= 0) ? uri.substring(0, q) : uri;
        return path.isEmpty() ? "/" : path;
    }

    private static Map<String, List<String>> parseQueryParams(String uri) {
        Map<String, List<String>> params = new HashMap<>();
        if (uri == null) return params;

        int questionMarkIndex = uri.indexOf('?');
        if (questionMarkIndex < 0 || questionMarkIndex == uri.length() - 1) {
            return params;
        }

        String query = uri.substring(questionMarkIndex + 1);

        int start = 0;
        while (start < query.length()) {

            int ampIndex = query.indexOf('&', start);
            if (ampIndex == -1) {
                ampIndex = query.length();
            }

            String pair = query.substring(start, ampIndex);

            int equalsIndex = pair.indexOf('=');

            if (pair.isEmpty()) {
                start = ampIndex + 1;
                continue;
            }

            String key;
            String value;

            if (equalsIndex >= 0) {
                key = pair.substring(0, equalsIndex);
                value = pair.substring(equalsIndex + 1);
            } else {
                key = pair;
                value = "";
            }

            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);

            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);

            start = ampIndex + 1;
        }

        return params;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    private IpFilter createIpFilterFromConfig(AppConfig.IpFilterConfig config) {
        IpFilter filter = new IpFilter();

        if ("ALLOWLIST".equalsIgnoreCase(config.mode())) {
            filter.setMode(IpFilter.FilterMode.ALLOWLIST);
        } else {
            filter.setMode(IpFilter.FilterMode.BLOCKLIST);
        }

        for (String ip : config.blockedIps()) {
            filter.addBlockedIp(ip);
        }

        for (String ip : config.allowedIps()) {
            filter.addAllowedIp(ip);
        }

        filter.init();
        return filter;
    }
}