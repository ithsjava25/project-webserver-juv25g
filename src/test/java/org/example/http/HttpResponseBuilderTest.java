package org.example.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HttpResponseBuilderTest {

    @Test
    void build_returnsValidHttpResponse() {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setBody("Hello");

        String result = builder.build();

        assertThat(result)
                .contains("HTTP/1.1 200 OK")
                .contains("Content-Length: 5")
                .contains("\r\n\r\n")
                .contains("Hello");
    }

    // UTF-8 content length för olika strängar
    @ParameterizedTest
    @CsvSource({
            "å,      2",   // 1 char, 2 bytes
            "åäö,    6",   // 3 chars, 6 bytes
            "Hello,  5",   // 5 chars, 5 bytes
            "'',     0",   // Empty string
            "€,      3"    // Euro sign, 3 bytes
    })
    @DisplayName("Should calculate correct Content-Length for various strings")
    void build_handlesUtf8ContentLength(String body, int expectedLength) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setBody(body);

        String result = builder.build();

        assertThat(result).contains("Content-Length: " + expectedLength);
    }

    @Test
    @DisplayName("Should set individual header")
    void setHeader_addsHeaderToResponse() {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setHeader("Content-Type", "text/html; charset=UTF-8");
        builder.setBody("Hello");

        String result = builder.build();

        assertThat(result).contains("Content-Type: text/html; charset=UTF-8");
    }

    @Test
    @DisplayName("Should set multiple headers")
    void setHeader_allowsMultipleHeaders() {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setHeader("Content-Type", "application/json");
        builder.setHeader("Cache-Control", "no-cache");
        builder.setBody("{}");

        String result = builder.build();

        assertThat(result)
                .contains("Content-Type: application/json")
                .contains("Cache-Control: no-cache");
    }

    @ParameterizedTest
    @CsvSource({
            "index.html,    text/html; charset=UTF-8",
            "page.htm,      text/html; charset=UTF-8",
            "style.css,     text/css; charset=UTF-8",
            "app.js,        application/javascript; charset=UTF-8",
            "data.json,     application/json; charset=UTF-8",
            "logo.png,      image/png",
            "photo.jpg,     image/jpeg",
            "image.jpeg,    image/jpeg",
            "icon.gif,      image/gif",
            "graphic.svg,   image/svg+xml",
            "favicon.ico,   image/x-icon",
            "doc.pdf,       application/pdf",
            "file.txt,      text/plain; charset=UTF-8",
            "config.xml,    application/xml; charset=UTF-8"
    })
    @DisplayName("Should auto-detect Content-Type from filename")
    void setContentTypeFromFilename_detectsVariousTypes(String filename, String expectedContentType) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setContentTypeFromFilename(filename);
        builder.setBody("test content");

        String result = builder.build();

        assertThat(result).contains("Content-Type: " + expectedContentType);
    }

    @ParameterizedTest(name = "{index} - Filename: {0} => Expected: {1}")
    @CsvSource(value = {
            "index.html,            text/html; charset=UTF-8",
            "style.css,             text/css; charset=UTF-8",
            "logo.png,              image/png",
            "doc.pdf,               application/pdf",
            "file.xyz,              application/octet-stream",
            "/var/www/index.html,   text/html; charset=UTF-8",
            "'',                    application/octet-stream",
            "null,                  application/octet-stream"
    }, nullValues = "null")
    @DisplayName("Should detect Content-Type from various filenames and edge cases")
    void setContentTypeFromFilename_allCases(String filename, String expectedContentType) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setContentTypeFromFilename(filename);
        builder.setBody("test");

        String result = builder.build();

        assertThat(result).contains("Content-Type: " + expectedContentType);
    }

    @ParameterizedTest
    @MethodSource("provideHeaderDuplicationScenarios")
    @DisplayName("Should not duplicate headers when manually set")
    void build_doesNotDuplicateHeaders(String headerName, String manualValue, String bodyContent) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setHeader(headerName, manualValue);
        builder.setBody(bodyContent);

        String result = builder.build();

        long count = result.lines()
                .filter(line -> line.startsWith(headerName + ":"))
                .count();

        assertThat(count).isEqualTo(1);
        assertThat(result).contains(headerName + ": " + manualValue);
    }

    private static Stream<Arguments> provideHeaderDuplicationScenarios() {
        return Stream.of(
                Arguments.of("Content-Length", "999", "Hello"),
                Arguments.of("Content-Length", "0", ""),
                Arguments.of("Content-Length", "12345", "Test content"),
                Arguments.of("Connection", "keep-alive", "Hello"),
                Arguments.of("Connection", "upgrade", "WebSocket data"),
                Arguments.of("Connection", "close", "Goodbye")
        );
    }

    @Test
    @DisplayName("setHeaders should preserve case-insensitive behavior")
    void setHeaders_preservesCaseInsensitivity() {
        HttpResponseBuilder builder = new HttpResponseBuilder();

        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "text/html");
        headers.put("cache-control", "no-cache");
        builder.setHeaders(headers);

        builder.setHeader("Content-Length", "100");
        builder.setBody("Hello");

        String result = builder.build();

        long count = result.lines()
                .filter(line -> line.toLowerCase().startsWith("content-length:"))
                .count();

        assertThat(count).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({
            "301, Moved Permanently",
            "302, Found",
            "304, Not Modified",
            "400, Bad Request",
            "401, Unauthorized",
            "403, Forbidden",
            "404, Not Found",
            "500, Internal Server Error",
            "502, Bad Gateway",
            "503, Service Unavailable"
    })
    @DisplayName("Should have correct reason phrases for common status codes")
    void build_correctReasonPhrases(int statusCode, String expectedReason) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setStatusCode(statusCode);
        builder.setBody("");

        String result = builder.build();

        assertThat(result).contains("HTTP/1.1 " + statusCode + " " + expectedReason);
    }

    // Redirect status codes
    @ParameterizedTest
    @CsvSource({
            "301, Moved Permanently, /new-page",
            "302, Found,             /temporary-page",
            "303, See Other,         /other-page",
            "307, Temporary Redirect, /temp-redirect",
            "308, Permanent Redirect, /perm-redirect"
    })
    @DisplayName("Should handle redirect status codes correctly")
    void build_handlesRedirectStatusCodes(int statusCode, String expectedReason, String location) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setStatusCode(statusCode);
        builder.setHeader("Location", location);
        builder.setBody("");

        String result = builder.build();

        assertThat(result)
                .contains("HTTP/1.1 " + statusCode + " " + expectedReason)
                .contains("Location: " + location)
                .doesNotContain("OK");
    }

    //  Error status codes
    @ParameterizedTest
    @CsvSource({
            "400, Bad Request",
            "401, Unauthorized",
            "403, Forbidden",
            "404, Not Found",
            "500, Internal Server Error",
            "502, Bad Gateway",
            "503, Service Unavailable"
    })
    @DisplayName("Should handle error status codes correctly")
    void build_handlesErrorStatusCodes(int statusCode, String expectedReason) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setStatusCode(statusCode);
        builder.setBody("Error message");

        String result = builder.build();

        assertThat(result)
                .contains("HTTP/1.1 " + statusCode + " " + expectedReason)
                .doesNotContain("OK");
    }

    //  Unknown status codes
    @ParameterizedTest
    @ValueSource(ints = {999, 123, 777, 100, 600})
    @DisplayName("Should handle unknown status codes gracefully")
    void build_handlesUnknownStatusCodes(int statusCode) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setStatusCode(statusCode);
        builder.setBody("");

        String result = builder.build();

        assertThat(result)
                .startsWith("HTTP/1.1 " + statusCode)
                .doesNotContain("OK");
    }

    @Test
    @DisplayName("Should auto-append headers when not manually set")
    void build_autoAppendsHeadersWhenNotSet() {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setBody("Hello");

        String result = builder.build();

        assertThat(result)
                .contains("Content-Length: 5")
                .contains("Connection: close");
    }

    @Test
    @DisplayName("Should allow custom headers alongside auto-generated ones")
    void build_combinesCustomAndAutoHeaders() {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setHeader("Content-Type", "text/html");
        builder.setHeader("Cache-Control", "no-cache");
        builder.setBody("Hello");

        String result = builder.build();

        assertThat(result)
                .contains("Content-Type: text/html")
                .contains("Cache-Control: no-cache")
                .contains("Content-Length: 5")
                .contains("Connection: close");
    }

    //  Case-insensitive header names
    @ParameterizedTest
    @CsvSource({
            "content-length,  100",
            "Content-Length,  100",
            "CONTENT-LENGTH,  100",
            "CoNtEnT-LeNgTh,  100"
    })
    @DisplayName("Should handle case-insensitive header names")
    void setHeader_caseInsensitive(String headerName, String headerValue) {
        HttpResponseBuilder builder = new HttpResponseBuilder();

        builder.setHeader(headerName, headerValue);
        builder.setBody("Hello");

        String result = builder.build();

        long count = result.lines()
                .filter(line -> line.toLowerCase().contains("content-length"))
                .count();

        assertThat(count).isEqualTo(1);
        assertThat(result.toLowerCase()).contains("content-length: " + headerValue.toLowerCase());
    }

    // Empty/null body
    @ParameterizedTest
    @CsvSource(value = {
            "'',   0",     // Empty string
            "null, 0"      // Null
    }, nullValues = "null")
    @DisplayName("Should handle empty and null body")
    void build_emptyAndNullBody(String body, int expectedLength) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setBody(body);

        String result = builder.build();

        assertThat(result)
                .contains("HTTP/1.1 200 OK")
                .contains("Content-Length: " + expectedLength);
    }

    //  Header override
    @ParameterizedTest
    @CsvSource({
            "Content-Type, text/plain,       text/html",
            "Content-Type, application/json, text/xml",
            "Connection,   keep-alive,       close",
            "Cache-Control, no-cache,        max-age=3600"
    })
    @DisplayName("Should override previous header value when set again")
    void setHeader_overridesPreviousValue(String headerName, String firstValue, String secondValue) {
        HttpResponseBuilder builder = new HttpResponseBuilder();
        builder.setHeader(headerName, firstValue);
        builder.setHeader(headerName, secondValue);  // Override
        builder.setBody("Test");

        String result = builder.build();

        assertThat(result)
                .contains(headerName + ": " + secondValue)
                .doesNotContain(headerName + ": " + firstValue);
    }

    //  för auto-append behavior
    @ParameterizedTest
    @MethodSource("provideAutoAppendScenarios")
    @DisplayName("Should auto-append specific headers when not manually set")
    void build_autoAppendsSpecificHeaders(String body, boolean setContentLength, boolean setConnection,
                                          String expectedContentLength, String expectedConnection) {
        HttpResponseBuilder builder = new HttpResponseBuilder();

        if (setContentLength) {
            builder.setHeader("Content-Length", "999");
        }
        if (setConnection) {
            builder.setHeader("Connection", "keep-alive");
        }

        builder.setBody(body);
        String result = builder.build();

        if (expectedContentLength != null) {
            assertThat(result).contains("Content-Length: " + expectedContentLength);
        }
        if (expectedConnection != null) {
            assertThat(result).contains("Connection: " + expectedConnection);
        }
    }

    private static Stream<Arguments> provideAutoAppendScenarios() {
        return Stream.of(
                // body, setContentLength, setConnection, expectedContentLength, expectedConnection
                Arguments.of("Hello", false, false, "5", "close"),           // Auto-append both
                Arguments.of("Hello", true, false, "999", "close"),          // Manual CL, auto Connection
                Arguments.of("Hello", false, true, "5", "keep-alive"),       // Auto CL, manual Connection
                Arguments.of("Hello", true, true, "999", "keep-alive"),      // Both manual
                Arguments.of("", false, false, "0", "close")                 // Empty body
        );
    }
}