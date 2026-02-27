package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleFilterWithCookieTest {

    @Test
    void testDefaultLocaleWhenNoHeaderOrCookie() {
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Map.of(),
                null
        );

        LocaleFilterWithCookie filter = new LocaleFilterWithCookie();

        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            assertEquals("en-US", LocaleFilterWithCookie.getCurrentLocale());
        });
    }

    @Test
    void testLocaleFromHeader() {
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Map.of("Accept-Language", "fr-FR,fr;q=0.9"),
                null
        );

        LocaleFilterWithCookie filter = new LocaleFilterWithCookie();

        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            assertEquals("fr-FR", LocaleFilterWithCookie.getCurrentLocale());
        });
    }

    @Test
    void testLocaleFromCookie() {
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Map.of("Cookie", "user-lang=es-ES; other=val"),
                null
        );

        LocaleFilterWithCookie filter = new LocaleFilterWithCookie();

        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            assertEquals("es-ES", LocaleFilterWithCookie.getCurrentLocale());
        });
    }

    @Test
    void testBlankCookieFallsBackToHeader() {
        HttpRequest request = new HttpRequest(
                "GET", "/", "HTTP/1.1",
                Map.of(
                        "Cookie", "user-lang=; other=value",
                        "Accept-Language", "fr-FR,fr;q=0.9"
                ),
                null
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
                null
        );

        LocaleFilterWithCookie filter = new LocaleFilterWithCookie();
        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            assertEquals("en-US", LocaleFilterWithCookie.getCurrentLocale());
        });
    }
}
