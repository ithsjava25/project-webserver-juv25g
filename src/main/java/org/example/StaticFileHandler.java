
package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

public class StaticFileHandler {
    private static final long FILE_SIZE_THRESHOLD = 1024 * 1024; // 1MB
    private static final int BUFFER_SIZE = 8192; // 8KB
    private static final String DEFAULT_WEB_ROOT = "www";

    private final String webRoot;
    private final CacheFilter cacheFilter;

    // Konstruktor för produktion
    public StaticFileHandler() {
        this(DEFAULT_WEB_ROOT, new CacheFilter());
    }

    // Konstruktor för testning
    public StaticFileHandler(String webRoot) {
        this(webRoot, new CacheFilter());
    }

    // Konstruktor för dependency injection
    public StaticFileHandler(String webRoot, CacheFilter cacheFilter) {
        this.webRoot = Objects.requireNonNull(webRoot, "webRoot kan inte vara null");
        this.cacheFilter = Objects.requireNonNull(cacheFilter, "cacheFilter kan inte vara null");
    }

    public void sendGetRequest(OutputStream outputStream, String uri) throws IOException {
        Objects.requireNonNull(outputStream, "outputStream kan inte vara null");
        Objects.requireNonNull(uri, "uri kan inte vara null");

        // Säkerhetskontroll - förhindra directory traversal
        validateUri(uri);

        File file = new File(webRoot, uri);

        // Verifiera att filen finns och är inom webRoot
        verifyFileExists(file);
        verifyFileIsWithinWebRoot(file);

        // Skicka headers först
        sendHttpHeaders(outputStream, file, uri);

        // Skicka body baserat på filstorlek
        long fileSize = file.length();
        if (fileSize < FILE_SIZE_THRESHOLD) {
            sendSmallFile(outputStream, uri, file);
        } else {
            sendLargeFile(outputStream, file);
        }
    }

    private void validateUri(String uri) throws IOException {
        if (uri.contains("..") || uri.contains("//")) {
            throw new IOException("Illegal URI: " + uri);
        }
    }

    private void verifyFileExists(File file) throws FileNotFoundException {
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }
    }

    private void verifyFileIsWithinWebRoot(File file) throws IOException {
        String webRootPath = new File(webRoot).getCanonicalPath();
        String filePath = file.getCanonicalPath();

        if (!filePath.startsWith(webRootPath)) {
            throw new IOException("Access denied: " + filePath);
        }
    }

    private void sendHttpHeaders(OutputStream outputStream, File file, String uri) throws IOException {
        HttpResponseBuilder response = new HttpResponseBuilder();
        String contentType = getContentType(uri);
        response.setHeaders(Map.of("Content-Type", contentType));
        response.setContentLength(file.length());

        PrintWriter writer = new PrintWriter(outputStream, false);
        writer.print(response.buildHeaders());
        writer.flush();
    }

    private String getContentType(String uri) {
        if (uri.endsWith(".html") || uri.endsWith(".htm")) {
            return "text/html; charset=utf-8";
        } else if (uri.endsWith(".css")) {
            return "text/css; charset=utf-8";
        } else if (uri.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        } else if (uri.endsWith(".json")) {
            return "application/json; charset=utf-8";
        } else if (uri.endsWith(".png")) {
            return "image/png";
        } else if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (uri.endsWith(".gif")) {
            return "image/gif";
        } else if (uri.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (uri.endsWith(".pdf")) {
            return "application/pdf";
        } else if (uri.endsWith(".txt")) {
            return "text/plain; charset=utf-8";
        }
        return "application/octet-stream";
    }

    private void sendSmallFile(OutputStream outputStream, String uri, File file) throws IOException {
        byte[] fileBytes = cacheFilter.getOrFetch(uri,
                path -> Files.readAllBytes(file.toPath())
        );

        if (fileBytes != null) {
            outputStream.write(fileBytes);
            outputStream.flush();
        }
    }

    private void sendLargeFile(OutputStream outputStream, File file) throws IOException {
        try (InputStream fileInputStream = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                if (bytesRead == BUFFER_SIZE){
                    outputStream.flush();
                }
            }
            outputStream.flush();
        }
    }
}
