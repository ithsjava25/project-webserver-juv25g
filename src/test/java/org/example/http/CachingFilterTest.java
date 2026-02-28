package org.example.http;

import org.example.FileResolver;
import org.example.config.ConfigLoader;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
/// //
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CachingFilterTest {

    @BeforeEach
    void setup() {
        ConfigLoader.loadOnce(Paths.get("src/test/resources/test-config.yml"));
    }

    @Test
    void shouldContinueChainWhenNoCacheHit() throws IOException {

        CachingFilter cachingFilter = new CachingFilter();

        File file = new File("www/ok.txt");
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), "hello");

        String resolvedPath = FileResolver.resolvePath("/ok.txt");

        HttpRequest request = new HttpRequest(
                "GET",
                "/does-not-exist",
                "HTTP/1.1",
                Map.of(),
                null,
                resolvedPath

        );
        HttpResponseBuilder response = new HttpResponseBuilder();

        TestFilterChain chain = new TestFilterChain();

        cachingFilter.doFilter(request, response, chain);

        assertThat(chain.called).isTrue();
    }

    @Test
    void shouldContinueChainWhenNoCachingHeaders() throws Exception {

        CachingFilter cachingFilter = new CachingFilter();

        String resolvedPath = FileResolver.resolvePath("/does-not-exist");

        File file = new File("www/ok.txt");
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), "hello");

        HttpRequest request = new HttpRequest(
                "GET",
                "/ok.txt",
                "HTTP/1.1",
                Map.of(),
                null,
                resolvedPath
        );

        HttpResponseBuilder response = new HttpResponseBuilder();
        TestFilterChain chain = new TestFilterChain();

        cachingFilter.doFilter(request, response, chain);

        assertThat(chain.called).isTrue();
    }
}
