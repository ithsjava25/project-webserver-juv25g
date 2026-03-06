package org.example.filter.redirect;

import org.example.filter.FilterChain;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedirectFilterTest {

    private static HttpRequest request(String path) {
        return new HttpRequest("GET", path, "HTTP/1.1", Map.of(), null);
    }

    private static String responseAsString(HttpResponseBuilder response) {
        return new String(response.build(), StandardCharsets.UTF_8);
    }

    @Test
    void returns_301_redirect_and_stops_pipeline() {
        RedirectFilter filter = new RedirectFilter(List.of(
                new RedirectRule(Pattern.compile("^/old-page$"), "/new-page", 301)
        ));

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        HttpResponseBuilder res = new HttpResponseBuilder();

        filter.doFilter(request("/old-page"), res, chain);

        String raw = responseAsString(res);
        assertThat(raw).contains("HTTP/1.1 301 Moved Permanently");
        assertThat(raw).contains("Location: /new-page");
        assertThat(chainCalled.get()).isFalse();
    }

    @Test
    void returns_302_redirect_and_stops_pipeline() {
        RedirectFilter filter = new RedirectFilter(List.of(
                new RedirectRule(Pattern.compile("^/temp$"), "https://example.com/temporary", 302)
        ));

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        HttpResponseBuilder res = new HttpResponseBuilder();

        filter.doFilter(request("/temp"), res, chain);

        String raw = responseAsString(res);
        assertThat(raw).contains("HTTP/1.1 302 Found");
        assertThat(raw).contains("Location: https://example.com/temporary");
        assertThat(chainCalled.get()).isFalse();
    }

    @Test
    void no_matching_rule_calls_next_in_chain() {
        RedirectFilter filter = new RedirectFilter(List.of(
                new RedirectRule(Pattern.compile("^/old-page$"), "/new-page", 301)
        ));

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> {
            chainCalled.set(true);
            res.setStatusCode(200);
            res.setBody("terminal");
        };

        HttpResponseBuilder res = new HttpResponseBuilder();

        filter.doFilter(request("/nope"), res, chain);

        String raw = responseAsString(res);
        assertThat(chainCalled.get()).isTrue();
        assertThat(raw).contains("HTTP/1.1 200 OK");
        assertThat(raw).doesNotContain("Location:");
    }

    @Test
    void wildcard_matching_docs_star() {
        var p = RedirectRulesLoader.compileSourcePattern("/docs/*");
        assertThat(p.matcher("/docs/test").matches()).isTrue();
        assertThat(p.matcher("/docs/any/path").matches()).isFalse();
        assertThat(p.matcher("/doc/test").matches()).isFalse();
    }

    @Test
    void regex_matching_via_loader_prefix() {
        var p = RedirectRulesLoader.compileSourcePattern("regex:^/docs/(v1|v2)$");
        assertThat(p.matcher("/docs/v1").matches()).isTrue();
        assertThat(p.matcher("/docs/v2").matches()).isTrue();
        assertThat(p.matcher("/docs/v3").matches()).isFalse();
    }

    @Test
    void redirect_rule_rejects_invalid_status_code() {
        assertThatThrownBy(() -> new RedirectRule(Pattern.compile("^/x$"), "/y", 307))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
