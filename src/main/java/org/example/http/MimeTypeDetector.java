package org.example.http;

import java.util.Map;

/**
 * Detects MIME types based on file extensions.
 * Used to set the Content-Type header when serving static files.
 */
public final class MimeTypeDetector {

    // Private constructor - utility class
    private MimeTypeDetector() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
            // HTML & Text
            Map.entry(".html", "text/html; charset=UTF-8"),
            Map.entry(".htm", "text/html; charset=UTF-8"),
            Map.entry(".css", "text/css; charset=UTF-8"),
            Map.entry(".js", "application/javascript; charset=UTF-8"),
            Map.entry(".json", "application/json; charset=UTF-8"),
            Map.entry(".xml", "application/xml; charset=UTF-8"),
            Map.entry(".txt", "text/plain; charset=UTF-8"),

            // Images
            Map.entry(".png", "image/png"),
            Map.entry(".jpg", "image/jpeg"),
            Map.entry(".jpeg", "image/jpeg"),
            Map.entry(".gif", "image/gif"),
            Map.entry(".svg", "image/svg+xml"),
            Map.entry(".ico", "image/x-icon"),
            Map.entry(".webp", "image/webp"),

            // Documents
            Map.entry(".pdf", "application/pdf"),

            // Video & Audio
            Map.entry(".mp4", "video/mp4"),
            Map.entry(".webm", "video/webm"),
            Map.entry(".mp3", "audio/mpeg"),
            Map.entry(".wav", "audio/wav"),

            // Fonts
            Map.entry(".woff", "font/woff"),
            Map.entry(".woff2", "font/woff2"),
            Map.entry(".ttf", "font/ttf"),
            Map.entry(".otf", "font/otf")
    );

    /**
     * Detects the MIME type of file based on its extension.
     *
     * @param filename the name of the file (t.ex., "index.html", "style.css")
     * @return the MIME type string (t.ex, "text/html; charset=UTF-8")
     */


    public static String detectMimeType(String filename) {

        String octet = "application/octet-stream";

        if (filename == null || filename.isEmpty()) {
            return octet;
        }

        // Find the last dot to get extension
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            // No extension or dot at end
            return octet;
        }

        String extension = filename.substring(lastDot).toLowerCase();
        return MIME_TYPES.getOrDefault(extension, octet);
    }
}