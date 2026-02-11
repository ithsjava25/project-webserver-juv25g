package org.example.httpparser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class HttpParseRequestLineTest {
    private HttpParseRequestLine httpParseRequestLine;

    @BeforeEach
    void setUp() {
        httpParseRequestLine = new HttpParseRequestLine();
    }

    @Test
    void testParserWithTestRequestLine() throws IOException {
        String testString = "GET / HTTP/1.1";

        InputStream in = new ByteArrayInputStream(testString.getBytes());

        httpParseRequestLine.parseHttpRequest(in);

        assertThat(httpParseRequestLine.getMethod()).isEqualTo("GET");
        assertThat(httpParseRequestLine.getUri()).isEqualTo("/");
        assertThat(httpParseRequestLine.getVersion()).isEqualTo("HTTP/1.1");
    }

    @Test
    void testParserThrowErrorWhenNull(){
        assertThatThrownBy(() -> httpParseRequestLine.parseHttpRequest(null)).isInstanceOf(NullPointerException.class);
    }


    @Test
    void testParserThrowErrorWhenEmpty(){
        InputStream in = new ByteArrayInputStream("".getBytes());
        Exception exception = assertThrows(
                IOException.class, () -> httpParseRequestLine.parseHttpRequest(in)
        );

        assertThat(exception.getMessage()).isEqualTo("HTTP Request Line is Null or Empty");
    }

    @Test
    void testParserThrowErrorWhenMethodIsInvalid(){
        String testString = "get / HTTP/1.1";
        InputStream in = new ByteArrayInputStream(testString.getBytes());

        Exception exception = assertThrows(
                IOException.class, () -> httpParseRequestLine.parseHttpRequest(in)
        );
        assertThat(exception.getMessage()).isEqualTo("Invalid HTTP method");
    }

    @Test
    void testParserThrowErrorWhenArrayLengthLessOrEqualsTwo(){
        String testString = "GET / ";
        InputStream in = new ByteArrayInputStream(testString.getBytes());
        Exception exception = assertThrows(
                IOException.class, () -> httpParseRequestLine.parseHttpRequest(in)
        );

        assertThat(exception.getMessage()).isEqualTo("HTTP Request Line is not long enough");
    }

}
