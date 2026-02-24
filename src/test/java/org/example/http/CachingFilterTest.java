package org.example.http;

import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CachingFilterTest {



    @Test
    void shouldReturn404WhenFileDoesNotExist() {

        CachingFilter cachingFilter = new CachingFilter();

        HttpRequest request = new HttpRequest(
                "GET",
                "/does-not-exist",
                "HTTP/1.1",
                Map.of(),
                null
        );

        HttpResponseBuilder response = new HttpResponseBuilder();

        TestFilterChain chain = new TestFilterChain();

        cachingFilter.doFilter(request, response, chain);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(chain.called).isFalse();
    }


    @Test
    void shouldContinueChainWhenNoCachingHeaders() throws Exception {

        CachingFilter cachingFilter = new CachingFilter();

        File file = new File("www/ok.txt");
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), "hello");

        HttpRequest request = new HttpRequest(
                "GET",
                "/ok.txt",
                "HTTP/1.1",
                Map.of(),
                null
        );

        HttpResponseBuilder response = new HttpResponseBuilder();
        TestFilterChain chain = new TestFilterChain();

        cachingFilter.doFilter(request, response, chain);

        assertThat(chain.called).isTrue();
    }
}
