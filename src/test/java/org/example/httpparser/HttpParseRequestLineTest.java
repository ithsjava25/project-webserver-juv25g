package org.example.httpparser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class HttpParseRequestLineTest {
    private HttpParser httpParseRequestLine;

    @BeforeEach
    void setUp() {
        httpParseRequestLine = new HttpParser();
    }

    @Test
    void testParserWithTestRequestLine() throws IOException {
        String testString = "GET / HTTP/1.1";

        InputStream in = new ByteArrayInputStream(testString.getBytes());
        httpParseRequestLine.setReader(in);
        httpParseRequestLine.parseRequest();

        assertThat(httpParseRequestLine.getMethod()).isEqualTo("GET");
        assertThat(httpParseRequestLine.getUri()).isEqualTo("/");
        assertThat(httpParseRequestLine.getVersion()).isEqualTo("HTTP/1.1");
    }

    @Test
    void testParserThrowErrorWhenNull(){
        assertThatThrownBy(() -> httpParseRequestLine.setReader(null)).isInstanceOf(NullPointerException.class);
    }


    @Test
    void testParserThrowErrorWhenEmpty(){
        InputStream in = new ByteArrayInputStream("".getBytes());
        httpParseRequestLine.setReader(in);
        Exception exception = assertThrows(
                IOException.class, () -> httpParseRequestLine.parseRequest()
        );

        assertThat(exception.getMessage()).isEqualTo("HTTP Request Line is Null or Empty");
    }

    @Test
    void testParserThrowErrorWhenMethodIsInvalid(){
        String testString = "get / HTTP/1.1";
        InputStream in = new ByteArrayInputStream(testString.getBytes());
        httpParseRequestLine.setReader(in);
        Exception exception = assertThrows(
                IOException.class, () -> httpParseRequestLine.parseRequest()
        );
        assertThat(exception.getMessage()).isEqualTo("Invalid HTTP method");
    }

    @Test
    void testParserThrowErrorWhenArrayLengthLessOrEqualsTwo(){
        String testString = "GET / ";
        InputStream in = new ByteArrayInputStream(testString.getBytes());
        httpParseRequestLine.setReader(in);
        Exception exception = assertThrows(
                IOException.class, () -> httpParseRequestLine.parseRequest()
        );

        assertThat(exception.getMessage()).isEqualTo("HTTP Request Line is not long enough");
    }

}
