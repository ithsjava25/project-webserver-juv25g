package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MimeTypeDetectorTest {
    private MimeTypeDetector detector;

    @BeforeEach
    void setUp() {
        detector = new MimeTypeDetector();
    }

    @Test
    void detectPdfMimeType() {
        String result = detector.detect("document.pdf");
        assertThat(result).isEqualTo("application/pdf");
    }

    @Test
    void detectJpgMimeType() {
        String result = detector.detect("photo.jpg");
        assertThat(result).isEqualTo("image/jpeg");
    }

    @Test
    void detectJpegMimeType() {
        String result = detector.detect("image.jpeg");
        assertThat(result).isEqualTo("image/jpeg");
    }

    @Test
    void detectPngMimeType() {
        String result = detector.detect("image.png");
        assertThat(result).isEqualTo("image/png");
    }

    @Test
    void detectHtmlMimeType() {
        String result = detector.detect("index.html");
        assertThat(result).isEqualTo("text/html");
    }

    @Test
    void detectJsonMimeType() {
        String result = detector.detect("data.json");
        assertThat(result).isEqualTo("application/json");
    }

    @Test
    void detectUnknownFileExtension() {
        String result = detector.detect("unknown.xyz");
        assertThat(result).isEqualTo("application/octet-stream");
    }

    @Test
    void detectFileWithoutExtension() {
        String result = detector.detect("README");
        assertThat(result).isEqualTo("application/octet-stream");
    }

    @Test
    void detectCasInsensitiveExtension() {
        String result1 = detector.detect("image.JPG");
        String result2 = detector.detect("image.jpg");
        assertThat(result1).isEqualTo(result2).isEqualTo("image/jpeg");
    }

    @Test
    void detectFileWithMultipleDots() {
        String result = detector.detect("archive.backup.zip");
        assertThat(result).isEqualTo("application/zip");
    }
}
