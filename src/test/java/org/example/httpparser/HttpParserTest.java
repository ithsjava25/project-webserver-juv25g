package org.example.httpparser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HttpParserTest {
    private HttpParser httpParser = new HttpParser();

    @Test
    void TestHttpParserForHeaders() throws IOException {
        String testInput = "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Type: text/plain\r\n" +
                "User-Agent: JUnit5\r\n" +
                "\r\n";
        InputStream in = new ByteArrayInputStream(testInput.getBytes(StandardCharsets.UTF_8));

        httpParser.parseHttp(in);

        Map<String, String> content = httpParser.getHeadersMap();

        assertNotNull(httpParser.getHeadersMap());
        assertThat(httpParser.getHeadersMap().get("Host")).isEqualTo("localhost");
        assertThat(httpParser.getHeadersMap().get("Content-Type")).isEqualTo("text/plain");
        assertThat(httpParser.getHeadersMap().get("User-Agent")).isEqualTo("JUnit5");
    }

    @Test
    void testParseHttp_EmptyInput() throws IOException {
        InputStream in = new ByteArrayInputStream("".getBytes());
        httpParser.parseHttp(in);

        assertTrue(httpParser.getHeadersMap().isEmpty());
    }

    @Test
    void testParseHttp_InvalidHeaderLine() throws IOException {
        String rawInput = "Host: localhost\r\n" +
                "InvalidLineWithoutColon\r\n" +
                "Accept: */*\r\n" +
                "\r\n";

        InputStream in = new ByteArrayInputStream(rawInput.getBytes(StandardCharsets.UTF_8));
        httpParser.parseHttp(in);

        assertEquals(2, httpParser.getHeadersMap().size());
        assertEquals("localhost", httpParser.getHeadersMap().get("Host"));
        assertEquals("*/*", httpParser.getHeadersMap().get("Accept"));
    }


}