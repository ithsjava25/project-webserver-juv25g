package org.example.filter;

import org.example.FileResolver;
import org.example.config.ConfigLoader;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleFilterTest {


    @BeforeEach
    void setUp() {
        ConfigLoader.loadOnce(Paths.get("src/test/resources/test-config.yml"));
    }

    @Test
    void shouldUseFirstLanguageFromHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Language", "sv-SE,sv;q=0.9,en;q=0.8");
        FileResolver.resolvePath("/whatever");

        HttpRequest request = new HttpRequest("GET", "/", "HTTP/1.1", headers, null, FileResolver.resolvePath("/whatever"));
        HttpResponseBuilder response = new HttpResponseBuilder();

        LocaleFilter filter = new LocaleFilter();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("sv-SE", LocaleFilter.getCurrentLocale());
        });

        assertEquals("en-US", LocaleFilter.getCurrentLocale());
    }

    @Test
    void shouldUseDefaultWhenHeaderMissing() {
        Map<String, String> headers = new HashMap<>();
        FileResolver.resolvePath("/whatever");

        HttpRequest request = new HttpRequest("GET", "/", "HTTP/1.1", headers, null, FileResolver.resolvePath("/whatever"));
        HttpResponseBuilder response = new HttpResponseBuilder();

        LocaleFilter filter = new LocaleFilter();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("en-US", LocaleFilter.getCurrentLocale());
        });
    }

    @Test
    void shouldUseDefaultWhenHeaderBlank() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Language", "   ");
        FileResolver.resolvePath("/whatever");

        HttpRequest request = new HttpRequest("GET", "/", "HTTP/1.1", headers, null, FileResolver.resolvePath("/whatever"));
        HttpResponseBuilder response = new HttpResponseBuilder();

        LocaleFilter filter = new LocaleFilter();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("en-US", LocaleFilter.getCurrentLocale());
        });
    }

    @Test
    void shouldHandleCaseInsensitiveHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept-language", "fr-FR");
        FileResolver.resolvePath("/whatever");

        HttpRequest request = new HttpRequest("GET", "/", "HTTP/1.1", headers, null, FileResolver.resolvePath("/whatever"));
        HttpResponseBuilder response = new HttpResponseBuilder();

        LocaleFilter filter = new LocaleFilter();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("fr-FR", LocaleFilter.getCurrentLocale());
        });
    }

    @Test
    void shouldUseDefaultWhenRequestIsNull() {
        LocaleFilter filter = new LocaleFilter();
        HttpResponseBuilder response = new HttpResponseBuilder();

        filter.doFilter(null, response, (req, res) -> {
            assertEquals("en-US", LocaleFilter.getCurrentLocale());
        });
    }

    @Test
    void shouldUseDefaultWhenHeadersAreEmpty() {
        FileResolver.resolvePath("/whatever");
        HttpRequest request = new HttpRequest("GET", "/", "HTTP/1.1", null, null, FileResolver.resolvePath("/whatever"));
        HttpResponseBuilder response = new HttpResponseBuilder();

        LocaleFilter filter = new LocaleFilter();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("en-US", LocaleFilter.getCurrentLocale());
        });
    }
}
