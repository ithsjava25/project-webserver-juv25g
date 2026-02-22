package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Map;

public class StaticFileHandler {
    private static final String WEB_ROOT = "www";
    private static final long FILE_SIZE_THRESHOLD = 1024 * 1024; // 1MB
    private static final int BUFFER_SIZE = 8192; // 8KB
    private static final String DEFAULT_WEB_ROOT = "www";
    
    private final String webRoot;
    private final CacheFilter cacheFilter = new CacheFilter();

    // Konstruktor för produktion
    public StaticFileHandler() {
        this.webRoot = DEFAULT_WEB_ROOT;
    }

    // Konstruktor för testning
    public StaticFileHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    public void sendGetRequest(OutputStream outputStream, String uri) throws IOException {
        File file = new File(webRoot, uri);
        
        // Skicka headers först
        sendHttpHeaders(outputStream, file);
        
        // Skicka body baserat på filstorlek
        long fileSize = file.length();
        if (fileSize < FILE_SIZE_THRESHOLD) {
            sendSmallFile(outputStream, uri, file);
        } else {
            sendLargeFile(outputStream, file);
        }
    }

    private void sendHttpHeaders(OutputStream outputStream, File file) throws IOException {
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setHeaders(Map.of("Content-Type", "text/html; charset=utf-8"));
        response.setContentLength(file.length());
        
        PrintWriter writer = new PrintWriter(outputStream, false);
        writer.print(response.buildHeaders());
        writer.flush();
    }

    private void sendSmallFile(OutputStream outputStream, String uri, File file) throws IOException {
        byte[] fileBytes = cacheFilter.getOrFetch(uri,
            path -> Files.readAllBytes(file.toPath())
        );
        
        outputStream.write(fileBytes);
        outputStream.flush();
    }

    private void sendLargeFile(OutputStream outputStream, File file) throws IOException {
        try (InputStream fileInputStream = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.flush();
        }
    }
}
