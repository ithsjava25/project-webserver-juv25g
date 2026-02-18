package org.example;

import org.example.http.HttpResponseBuilder;

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
        // Security: Prevent path traversal attacks (e.g. GET /../../etc/passwd)
        File root = new File(WEB_ROOT).getCanonicalFile();
        File file = new File(root, uri).getCanonicalFile();

        if (!file.toPath().startsWith(root.toPath())) {
            fileBytes = "403 Forbidden".getBytes();
            statusCode = 403;
            return;
        }

        if (file.exists()) {
            fileBytes = Files.readAllBytes(file.toPath());
            statusCode = 200;
        } else {
            File errorFile = new File(WEB_ROOT, "pageNotFound.html");
            if (errorFile.exists()) {
                fileBytes = Files.readAllBytes(errorFile.toPath());
            } else {
                fileBytes = "404 Not Found".getBytes();
            }
            statusCode = 404;
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