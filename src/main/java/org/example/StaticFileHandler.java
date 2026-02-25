package org.example;

import org.example.http.HttpResponseBuilder;
import static org.example.http.HttpResponseBuilder.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class StaticFileHandler {
    private final String WEB_ROOT;
    private byte[] fileBytes;
    private int statusCode;

    // Constructor for production
    public StaticFileHandler() {
        WEB_ROOT = "www";
    }

    // Constructor for tests, otherwise the www folder won't be seen
    public StaticFileHandler(String webRoot) {
        WEB_ROOT = webRoot;
    }

    private void handleGetRequest(String uri) throws IOException {
        // Sanitize URI
        int q = uri.indexOf('?');
        if (q >= 0) uri = uri.substring(0, q);
        int h = uri.indexOf('#');
        if (h >= 0) uri = uri.substring(0, h);
        uri = uri.replace("\0", "");
        if (uri.startsWith("/")) uri = uri.substring(1);

        // Path traversal check
        File root = new File(WEB_ROOT).getCanonicalFile();
        File file = new File(root, uri).getCanonicalFile();
        if (!file.toPath().startsWith(root.toPath())) {
            fileBytes = "403 Forbidden".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            statusCode = SC_FORBIDDEN;
            return;
        }

        // Read file
        if (file.isFile()) {
            fileBytes = Files.readAllBytes(file.toPath());
            statusCode = SC_OK;
        } else {
            File errorFile = new File(WEB_ROOT, "pageNotFound.html");
            if (errorFile.isFile()) {
                fileBytes = Files.readAllBytes(errorFile.toPath());
            } else {
                fileBytes = "404 Not Found".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
            statusCode = SC_NOT_FOUND;
        }
    }

    public void sendGetRequest(OutputStream outputStream, String uri) throws IOException {
        handleGetRequest(uri);
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(statusCode);
        // Use MimeTypeDetector instead of hardcoded text/html
        response.setContentTypeFromFilename(uri);
        response.setBody(fileBytes);
        outputStream.write(response.build());
        outputStream.flush();
    }
}
