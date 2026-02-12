package org.example.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class MimeTypeDetectorTest {

    @Test
    @DisplayName("Should detect HTML files")
    void detectMimeType_html() {
        assertThat(MimeTypeDetector.detectMimeType("index.html"))
                .isEqualTo("text/html; charset=UTF-8");

        assertThat(MimeTypeDetector.detectMimeType("page.htm"))
                .isEqualTo("text/html; charset=UTF-8");
    }

    @Test
    @DisplayName("Should detect CSS files")
    void detectMimeType_css() {
        assertThat(MimeTypeDetector.detectMimeType("style.css"))
                .isEqualTo("text/css; charset=UTF-8");
    }

    @Test
    @DisplayName("Should detect JavaScript files")
    void detectMimeType_javascript() {
        assertThat(MimeTypeDetector.detectMimeType("app.js"))
                .isEqualTo("application/javascript; charset=UTF-8");
    }

    @Test
    @DisplayName("Should detect JSON files")
    void detectMimeType_json() {
        assertThat(MimeTypeDetector.detectMimeType("data.json"))
                .isEqualTo("application/json; charset=UTF-8");
    }

    @Test
    @DisplayName("Should detect PNG images")
    void detectMimeType_png() {
        assertThat(MimeTypeDetector.detectMimeType("logo.png"))
                .isEqualTo("image/png");
    }

    @Test
    @DisplayName("Should detect JPEG images with .jpg extension")
    void detectMimeType_jpg() {
        assertThat(MimeTypeDetector.detectMimeType("photo.jpg"))
                .isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("Should detect JPEG images with .jpeg extension")
    void detectMimeType_jpeg() {
        assertThat(MimeTypeDetector.detectMimeType("photo.jpeg"))
                .isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("Should detect PDF files")
    void detectMimeType_pdf() {
        assertThat(MimeTypeDetector.detectMimeType("document.pdf"))
                .isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("Should be case-insensitive")
    void detectMimeType_caseInsensitive() {
        assertThat(MimeTypeDetector.detectMimeType("INDEX.HTML"))
                .isEqualTo("text/html; charset=UTF-8");

        assertThat(MimeTypeDetector.detectMimeType("Style.CSS"))
                .isEqualTo("text/css; charset=UTF-8");

        assertThat(MimeTypeDetector.detectMimeType("PHOTO.PNG"))
                .isEqualTo("image/png");
    }

    @Test
    @DisplayName("Should return default MIME type for unknown extensions")
    void detectMimeType_unknownExtension() {
        assertThat(MimeTypeDetector.detectMimeType("file.xyz"))
                .isEqualTo("application/octet-stream");

        assertThat(MimeTypeDetector.detectMimeType("document.unknown"))
                .isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("Should handle files without extension")
    void detectMimeType_noExtension() {
        assertThat(MimeTypeDetector.detectMimeType("README"))
                .isEqualTo("application/octet-stream");

        assertThat(MimeTypeDetector.detectMimeType("Makefile"))
                .isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("Should handle null filename")
    void detectMimeType_null() {
        assertThat(MimeTypeDetector.detectMimeType(null))
                .isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("Should handle empty filename")
    void detectMimeType_empty() {
        assertThat(MimeTypeDetector.detectMimeType(""))
                .isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("Should handle filename ending with dot")
    void detectMimeType_endsWithDot() {
        assertThat(MimeTypeDetector.detectMimeType("file."))
                .isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("Should handle path with directories")
    void detectMimeType_withPath() {
        assertThat(MimeTypeDetector.detectMimeType("/var/www/index.html"))
                .isEqualTo("text/html; charset=UTF-8");

        assertThat(MimeTypeDetector.detectMimeType("css/styles/main.css"))
                .isEqualTo("text/css; charset=UTF-8");
    }

    @Test
    @DisplayName("Should handle multiple dots in filename")
    void detectMimeType_multipleDots() {
        assertThat(MimeTypeDetector.detectMimeType("jquery.min.js"))
                .isEqualTo("application/javascript; charset=UTF-8");

        assertThat(MimeTypeDetector.detectMimeType("bootstrap.bundle.min.css"))
                .isEqualTo("text/css; charset=UTF-8");
    }

    // Parametriserad test för många filtyper på en gång
    @ParameterizedTest
    @CsvSource({
            "test.html, text/html; charset=UTF-8",
            "style.css, text/css; charset=UTF-8",
            "app.js, application/javascript; charset=UTF-8",
            "data.json, application/json; charset=UTF-8",
            "image.png, image/png",
            "photo.jpg, image/jpeg",
            "doc.pdf, application/pdf",
            "icon.svg, image/svg+xml",
            "favicon.ico, image/x-icon",
            "video.mp4, video/mp4",
            "audio.mp3, audio/mpeg"
    })
    @DisplayName("Should detect common file types")
    void detectMimeType_commonTypes(String filename, String expectedMimeType) {
        assertThat(MimeTypeDetector.detectMimeType(filename))
                .isEqualTo(expectedMimeType);
    }
}