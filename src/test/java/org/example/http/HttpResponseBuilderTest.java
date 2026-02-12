package org.example.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

    class HttpResponseBuilderTest {

        /**
         * Verifies that build produces a valid HTTP response string!
         * Status line is present
         * Content-Length header is generated
         * The response body is included
         */

        @Test
        void build_returnsValidHttpResponse() {

            HttpResponseBuilder builder = new HttpResponseBuilder();

            builder.setBody("Hello");

            String result = builder.build();

            assertThat(result).contains("HTTP/1.1 200 OK");
            assertThat(result).contains("Content-Length: 5");
            assertThat(result).contains("\r\n\r\n");
            assertThat(result).contains("Hello");

        }

        // Verifies that Content-Length is calculated using UTF-8 byte length!
        //
        @Test
        void build_handlesUtf8ContentLength() {
            HttpResponseBuilder builder = new HttpResponseBuilder();

            builder.setBody("å");

            String result = builder.build();

            assertThat(result).contains("Content-Length: 2");

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

            assertThat(result).contains("Content-Type: application/json");
            assertThat(result).contains("Cache-Control: no-cache");
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
                "file.xyz,              application/octet-stream", // Okänd ändelse
                "/var/www/index.html,   text/html; charset=UTF-8", // Med sökväg
                "'',                    application/octet-stream", // Tom sträng
                "null,                  application/octet-stream"  // Null-värde
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

            // Count occurrences of the header
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

    }
