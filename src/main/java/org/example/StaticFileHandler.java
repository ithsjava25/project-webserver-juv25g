package org.example;

import org.example.http.HttpResponseBuilder;
import static org.example.http.HttpResponseBuilder.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

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
        // Sanitize URI
        uri = sanitizeUri(uri);

        // Path traversal check
        File root = new File(WEB_ROOT).getCanonicalFile();
        File file = new File(root, uri).getCanonicalFile();
        
        if (!file.toPath().startsWith(root.toPath())) {
            sendErrorResponse(outputStream, SC_FORBIDDEN, "403 Forbidden", uri);
            return;
        }

        // Send file or error
        if (file.isFile()) {
            sendFile(outputStream, file, uri);
        } else {
            File errorFile = new File(WEB_ROOT, "pageNotFound.html");
            if (errorFile.isFile()) {
                sendErrorFile(outputStream, errorFile);
            } else {
                sendErrorResponse(outputStream, SC_NOT_FOUND, "404 Not Found", uri);
            }
        }
    }

    private String sanitizeUri(String uri) {
        // Remove query string and fragment
        int q = uri.indexOf('?');
        if (q >= 0) uri = uri.substring(0, q);
        int h = uri.indexOf('#');
        if (h >= 0) uri = uri.substring(0, h);
        
        // Remove null bytes and leading slashes
        uri = uri.replace("\0", "").replaceAll("^/+", "");
        
        return uri;
    }

    private void sendFile(OutputStream out, File file, String uri) throws IOException {
        long fileSize = file.length();
        
        // Send headers
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(SC_OK);
        response.setContentTypeFromFilename(uri);
        response.setContentLength(fileSize);
        
        byte[] headers = response.buildHeaders();
        out.write(headers);
        
        // Send body: stream large files, cache small files
        if (fileSize < FILE_SIZE_THRESHOLD) {
            streamSmallFile(out, file);
        } else {
            streamLargeFile(out, file);
        }
    }

    private void streamSmallFile(OutputStream out, File file) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        out.write(fileBytes);
        out.flush();
    }

    private void streamLargeFile(OutputStream out, File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    private void sendErrorFile(OutputStream out, File errorFile) throws IOException {
        byte[] errorBytes = Files.readAllBytes(errorFile.toPath());
        
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(SC_NOT_FOUND);
        response.setContentTypeFromFilename("error.html");
        response.setBody(errorBytes);
        
        out.write(response.build());
        out.flush();
    }

    private void sendErrorResponse(OutputStream out, int statusCode, String message, String uri) throws IOException {
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(statusCode);
        response.setContentTypeFromFilename(uri);
        response.setBody(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        
        out.write(response.build());
        out.flush();
    }
}
