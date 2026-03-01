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

import java.io.OutputStream;
import java.nio.file.NoSuchFileException;

public class ConnectionHandler implements AutoCloseable {
    private final Socket client;
    private final List<Filter> filters;
    private final String webRoot;
    private final FileCache fileCache;

    public ConnectionHandler(Socket client, FileCache fileCache) {
        this(client, "www", fileCache);
    }

    public ConnectionHandler(Socket client, String webRoot, FileCache fileCache) {
        this.client = client;
        this.webRoot = webRoot;
        this.fileCache = fileCache;
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
        HttpParser parser = parseRequest();
        HttpRequest request = buildHttpRequest(parser);

        if (isForbiddenByFilters(request)) return;
        if (isPathTraversal(parser.getUri())) return;

        serveFile(parser.getUri());
    }

    private HttpParser parseRequest() throws IOException {
        HttpParser parser = new HttpParser();
        parser.setReader(client.getInputStream());
        parser.parseRequest();
        parser.parseHttp();
        return parser;
    }

    private HttpRequest buildHttpRequest(HttpParser parser) {
        HttpRequest request = new HttpRequest(
                parser.getMethod(), parser.getUri(), parser.getVersion(),
                parser.getHeadersMap(), ""
        );
        request.setAttribute("clientIp", client.getInetAddress().getHostAddress());
        return request;
    }

    private boolean isForbiddenByFilters(HttpRequest request) throws IOException {
        HttpResponseBuilder response = new HttpResponseBuilder();
        new FilterChainImpl(filters).doFilter(request, response);

        int status = response.getStatusCode();
        if (status == HttpResponseBuilder.SC_FORBIDDEN || status == HttpResponseBuilder.SC_BAD_REQUEST) {
            client.getOutputStream().write(response.build());
            client.getOutputStream().flush();
            return true;
        }
        return false;
    }

    private boolean isPathTraversal(String uri) throws IOException {
        if (uri.contains("..")) {
            sendErrorResponse(client.getOutputStream(), 403, "Forbidden");
            return true;
        }
        return false;
    }

    private void serveFile(String uri) throws IOException {
        StaticFileHandler sfh = new StaticFileHandler(webRoot, fileCache);
        try {
            sfh.sendGetRequest(client.getOutputStream(), uri);
        } catch (NoSuchFileException e) {
            sendErrorResponse(client.getOutputStream(), 404, "Not Found");
        } catch (Exception e) {
            sendErrorResponse(client.getOutputStream(), 500, "Internal Server Error");
        }
    }

    private void sendErrorResponse(OutputStream out, int statusCode, String message) throws IOException {
        out.write(HttpResponseBuilder.createErrorResponse(statusCode, message));
        out.flush();
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