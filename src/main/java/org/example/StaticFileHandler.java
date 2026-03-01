
package org.example;

import org.example.http.HttpResponseBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler {
    private static final String DEFAULT_WEB_ROOT = "www";
    private final String webRoot;
    private final FileCache fileCache;

    public StaticFileHandler(FileCache fileCache) {
        this(DEFAULT_WEB_ROOT, fileCache);
    }

    public StaticFileHandler(String webRoot, FileCache fileCache) {
        this.webRoot = webRoot;
        this.fileCache = fileCache;
    }

    public void sendGetRequest(OutputStream outputStream, String uri) throws IOException {
        String sanitizedUri = sanitizeUri(uri);
        Path filePath = Path.of(webRoot, sanitizedUri);

        // Använd fullständig sökväg som cache-nyckel
        String cacheKey = filePath.toString();
        byte[] fileBytes = fileCache.get(cacheKey);

        if (fileBytes == null) {
            fileBytes = Files.readAllBytes(filePath);
            fileCache.put(cacheKey, fileBytes);
        }

        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setContentTypeFromFilename(sanitizedUri);
        response.setBody(fileBytes);
        outputStream.write(response.build());
        outputStream.flush();
    }

    private String sanitizeUri(String uri) {
        if (uri == null || uri.isEmpty() || uri.equals("/")) return "index.html";

        // Ta bort query, anchors, start-snedstreck och null-bytes
        String cleanUri = uri.split("[?#]")[0]
                .replaceAll("^/+", "")
                .replace("\0", "");

        return cleanUri.isEmpty() ? "index.html" : cleanUri;
    }

    public void clearCache() {
        fileCache.clear();
    }
}
