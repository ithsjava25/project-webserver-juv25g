package org.example.filter;

import org.example.FileResolver;
import org.example.config.ConfigLoader;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleFilterWithCookieTest {


    @BeforeEach
    void setUp() {
        ConfigLoader.loadOnce(Paths.get("src/test/resources/test-config.yml"));
    }



    @Test
    void testDefaultLocaleWhenNoHeaderOrCookie() {

        FileResolver.resolvePath("/whatever");
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Map.of(),
                null,
                FileResolver.resolvePath("/whatever")
        );

        LocaleFilterWithCookie filter = new LocaleFilterWithCookie();

        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            assertEquals("en-US", LocaleFilterWithCookie.getCurrentLocale());
        });
    }

    @Test
    void testLocaleFromHeader() {
        FileResolver.resolvePath("/whatever");
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Map.of("Accept-Language", "fr-FR,fr;q=0.9"),
                null,
                FileResolver.resolvePath("/whatever")
        );

        LocaleFilterWithCookie filter = new LocaleFilterWithCookie();

        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            assertEquals("fr-FR", LocaleFilterWithCookie.getCurrentLocale());
        });
    }

    @Test
    void testLocaleFromCookie() {
        FileResolver.resolvePath("/whatever");
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Map.of("Cookie", "user-lang=es-ES; other=val"),
                null,
                FileResolver.resolvePath("/whatever")
        );

        LocaleFilterWithCookie filter = new LocaleFilterWithCookie();

        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            assertEquals("es-ES", LocaleFilterWithCookie.getCurrentLocale());
        });
    }

    @Test
    void testBlankCookieFallsBackToHeader() {
        FileResolver.resolvePath("/whatever");
        HttpRequest request = new HttpRequest(
                "GET", "/", "HTTP/1.1",
                Map.of(
                        "Cookie", "user-lang=; other=value",
                        "Accept-Language", "fr-FR,fr;q=0.9"
                ),
                null,
                FileResolver.resolvePath("/whatever")
        );

        LocaleFilterWithCookie filter = new LocaleFilterWithCookie();
        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            assertEquals("fr-FR", LocaleFilterWithCookie.getCurrentLocale());
        });
    }

    @Test
    void testCookieWithWhitespaceOnly() {
        HttpRequest request = new HttpRequest(
                "GET", "/", "HTTP/1.1",
                Map.of(
                        "Cookie", "user-lang=   "
                ),
                null,
                FileResolver.resolvePath("/whatever")
        );

        LocaleFilterWithCookie filter = new LocaleFilterWithCookie();
        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            assertEquals("en-US", LocaleFilterWithCookie.getCurrentLocale());
        });
    }
}
