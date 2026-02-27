package org.example;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.example.http.HttpResponseBuilder.*;

public class StaticFileHandler implements org.example.server.TerminalHandler {
    private final String webRoot;
    public static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain; charset=utf-8";

    // Constructor for production
    public StaticFileHandler() {
        webRoot = "www";
    }

    // Constructor for tests, otherwise the www folder won't be seen
    public StaticFileHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    private void handleGetRequest(HttpRequest request, HttpResponseBuilder response) {
        byte[] fileBytes;
        if (!"GET".equals(request.getMethod())) {
            fileBytes = "405 Method Not Allowed".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            response.setContentType(TEXT_PLAIN_CHARSET_UTF_8);
            response.setStatusCode(SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", "GET");
            response.setBody(fileBytes);
            return;
        }

        String uri = request.getPath();
        if (uri == null) {
            uri = "";
        }

        // Sanitize URI
        int q = uri.indexOf('?');
        if (q >= 0) uri = uri.substring(0, q);
        int h = uri.indexOf('#');
        if (h >= 0) uri = uri.substring(0, h);
        uri = uri.replace("\0", "");

        uri = defaultFile(uri);

        // Path traversal check
        File root;
        File file;
        try {
            root = new File(webRoot).getCanonicalFile();
            file = new File(root, uri).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!file.toPath().startsWith(root.toPath())) {
            fileBytes = "403 Forbidden".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            response.setContentType(TEXT_PLAIN_CHARSET_UTF_8);
            response.setStatusCode(SC_FORBIDDEN);
        } else if (file.isFile()) {
            try {
                fileBytes = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            response.setContentTypeFromFilename(file.toString());
            response.setStatusCode(SC_OK);
        } else {
            File errorFile = new File(webRoot, "pageNotFound.html");
            if (errorFile.isFile()) {
                try {
                    fileBytes = Files.readAllBytes(errorFile.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                response.setContentTypeFromFilename(errorFile.toString());
            } else {
                fileBytes = "404 Not Found".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                response.setContentType(TEXT_PLAIN_CHARSET_UTF_8);
            }
            response.setStatusCode(SC_NOT_FOUND);
        }
        response.setBody(fileBytes);
    }

    @Override
    public void handle(HttpRequest request, HttpResponseBuilder response) {
        handleGetRequest(request, response);
    }

    private String defaultFile(String uri) {
        if (uri == null || uri.isBlank()) {
            return "index.html";
        }
        String normalized = uri;
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isEmpty()) {
            return "index.html";
        }
        if (normalized.endsWith("/")) {
            normalized = normalized + "index.html";
        }
        return normalized;
    }
}
