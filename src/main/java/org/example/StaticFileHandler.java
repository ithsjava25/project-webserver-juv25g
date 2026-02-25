package org.example;

import org.example.http.HttpResponseBuilder;
import static org.example.http.HttpResponseBuilder.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public class StaticFileHandler {
    private static final long FILE_SIZE_THRESHOLD = 1024 * 1024; // 1MB
    private static final int BUFFER_SIZE = 8192; // 8KB

    private final String WEB_ROOT;

    public StaticFileHandler() {
        WEB_ROOT = "www";
    }

    public StaticFileHandler(String webRoot) {
        WEB_ROOT = webRoot;
    }

    public void sendGetRequest(OutputStream outputStream, String uri) throws IOException {
        Objects.requireNonNull(outputStream, "outputStream kan inte vara null");
        Objects.requireNonNull(uri, "uri kan inte vara null");

        // Null-byte detection BEFORE sanitizing
        if (uri.contains("\0")) {
            sendErrorResponse(outputStream, SC_FORBIDDEN, "403 Forbidden");
            return;
        }

        // Sanitize URI (now safe - no null-bytes to remove)
        uri = sanitizeUri(uri);

        // Path traversal check
        File root = new File(WEB_ROOT).getCanonicalFile();
        File file = new File(root, uri).getCanonicalFile();

        if (!file.toPath().startsWith(root.toPath())) {
            sendErrorResponse(outputStream, SC_FORBIDDEN, "403 Forbidden");
            return;
        }

        // Send file or error
        if (file.isFile()) {
            sendFile(outputStream, file, uri);
        } else {
            // Return 404, NOT the error file with 200 OK
            sendErrorResponse(outputStream, SC_NOT_FOUND, "404 Not Found");
        }
    }

    private String sanitizeUri(String uri) {
        int q = uri.indexOf('?');
        if (q >= 0) uri = uri.substring(0, q);
        int h = uri.indexOf('#');
        if (h >= 0) uri = uri.substring(0, h);

        uri = uri.replaceAll("^/+", "");  // Removed .replace("\0", "") since we check earlier
        return uri;
    }

    private void sendFile(OutputStream out, File file, String uri) throws IOException {
        long fileSize = file.length();

        // For small files, use cached approach
        if (fileSize < FILE_SIZE_THRESHOLD) {
            sendFileWithCache(out, file, uri);
        } else {
            // For large files, stream with headers
            sendFileStreamed(out, file, uri, fileSize);
        }
    }

    private void sendFileWithCache(OutputStream out, File file, String uri) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(SC_OK);
        response.setContentTypeFromFilename(uri);
        response.setBody(fileBytes);

        out.write(response.build());
        out.flush();
    }

    private void sendFileStreamed(OutputStream out, File file, String uri, long fileSize) throws IOException {
        // Send headers first
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(SC_OK);
        response.setContentTypeFromFilename(uri);
        response.setHeader("Content-Length", String.valueOf(fileSize));
        response.setHeader("Connection", "close");

        byte[] headers = response.buildHeaders();
        out.write(headers);

        // Stream body
        try (InputStream in = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    private void sendErrorResponse(OutputStream out, int statusCode, String message) throws IOException {
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(statusCode);
        response.setContentTypeFromFilename("error.html");
        response.setBody(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        out.write(response.build());
        out.flush();
    }
}
