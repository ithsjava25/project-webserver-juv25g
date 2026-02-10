package org.example.httptest;

import org.example.http.HttpResponseBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HttpResponseBuilderTest {


    /**
     * Verifies that build produces a valid HTTP response string!

     *  Status line is present
     *  Content-Length header is generated
     *  The response body is included
     */
    @Test
    public void build_returnsValidHttpResponse() {

        HttpResponseBuilder builder = new HttpResponseBuilder();

        builder.setBody("Hello");

        String result = builder.build();

        assertThat(result).contains("HTTP/1.1 200 OK");
        assertThat(result).contains("Content-Length");
        assertThat(result).contains("\r\n\r\n");
        assertThat(result).contains("Hello");

    }

    // Verifies that Content-Length is calculated using UTF-8 byte length!
    //
    @Test
    public void build_handlesUtf8ContentLength() {
        HttpResponseBuilder builder = new HttpResponseBuilder();

        builder.setBody("å");

        String result = builder.build();

        assertThat(result).contains("Content-Length: 2");

    }
}
