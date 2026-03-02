package org.example.filter;

import org.example.FileResolver;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleStatsFilterTest {

    @BeforeEach
    void resetStats() {
        LocaleStatsFilter.resetStatsForTests();
    }

    @Test
    void testSingleRequestUpdatesStats() {
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
        LocaleStatsFilter statsFilter = new LocaleStatsFilter();

        filter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            statsFilter.doFilter(req, res, (r, s) -> {});
        });

        Map<String, Integer> stats = LocaleStatsFilter.getLocaleStats();
        assertEquals(1, stats.size());
        assertEquals(1, stats.get("fr-fr")); // only change: lowercased
    }

    @Test
    void testMultipleRequestsSameLocale() {
        FileResolver.resolvePath("/whatever");
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Map.of("Accept-Language", "sv-SE,sv;q=0.9"),
                null,
                FileResolver.resolvePath("/whatever")
        );

        LocaleFilterWithCookie localeFilter = new LocaleFilterWithCookie();
        LocaleStatsFilter statsFilter = new LocaleStatsFilter();

        for (int i = 0; i < 3; i++) {
            localeFilter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
                statsFilter.doFilter(req, res, (r, s) -> {});
            });
        }

        Map<String, Integer> stats = LocaleStatsFilter.getLocaleStats();
        assertEquals(1, stats.size());
        assertEquals(3, stats.get("sv-se")); // only change: lowercased
    }

    @Test
    void testMultipleRequestsDifferentLocales() {
        FileResolver.resolvePath("/whatever");
        HttpRequest request1 = new HttpRequest(
                "GET", "/", "HTTP/1.1",
                Map.of("Accept-Language", "fr-FR"), null,
                FileResolver.resolvePath("/whatever")
        );
        HttpRequest request2 = new HttpRequest(
                "GET", "/", "HTTP/1.1",
                Map.of("Accept-Language", "es-ES"), null,
                FileResolver.resolvePath("/whatever")
        );

        LocaleFilterWithCookie localeFilter = new LocaleFilterWithCookie();
        LocaleStatsFilter statsFilter = new LocaleStatsFilter();

        localeFilter.doFilter(request1, new HttpResponseBuilder(), (req, res) -> {
            statsFilter.doFilter(req, res, (r, s) -> {});
        });
        localeFilter.doFilter(request2, new HttpResponseBuilder(), (req, res) -> {
            statsFilter.doFilter(req, res, (r, s) -> {});
        });

        Map<String, Integer> stats = LocaleStatsFilter.getLocaleStats();
        assertEquals(2, stats.size());
        assertEquals(1, stats.get("fr-fr")); // only change: lowercased
        assertEquals(1, stats.get("es-es")); // only change: lowercased
    }

    @Test
    void testNoLocaleFallsBackToDefault() {
        FileResolver.resolvePath("/whatever");
        HttpRequest request = new HttpRequest(
                "GET", "/", "HTTP/1.1",
                Map.of(), null,
                FileResolver.resolvePath("/whatever")
        );

        LocaleFilterWithCookie localeFilter = new LocaleFilterWithCookie();
        LocaleStatsFilter statsFilter = new LocaleStatsFilter();

        localeFilter.doFilter(request, new HttpResponseBuilder(), (req, res) -> {
            statsFilter.doFilter(req, res, (r, s) -> {});
        });

        Map<String, Integer> stats = LocaleStatsFilter.getLocaleStats();
        assertEquals(1, stats.size());
        assertEquals(1, stats.get("en-us")); // only change: lowercased
    }
}
