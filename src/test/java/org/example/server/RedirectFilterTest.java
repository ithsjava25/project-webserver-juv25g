package org.example.server;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedirectFilterTest {

    @Test
    void returns_301_redirect_and_stops_pipeline() {
        RedirectFilter filter = new RedirectFilter(List.of(
                new RedirectRule(Pattern.compile("^/old-page$"), "/new-page", 301)
        ));

        AtomicBoolean terminalCalled = new AtomicBoolean(false);
        FilterChain chain = new FilterChain(new HttpFilter[] {filter}, (req, res) -> terminalCalled.set(true));

        HttpRequest req = new HttpRequest("GET", "/old-page");
        HttpResponse res = new HttpResponse();

        chain.doFilter(req, res);

        assertThat(res.status()).isEqualTo(301);
        assertThat(res.headers()).containsEntry("Location", "/new-page");
        assertThat(terminalCalled.get()).isFalse();
    }

    @Test
    void returns_302_redirect() {
        RedirectFilter filter = new RedirectFilter(List.of(
                new RedirectRule(Pattern.compile("^/temp$"), "https://example.com/temporary", 302)
        ));

        FilterChain chain = new FilterChain(new HttpFilter[] {filter}, (req, res) -> res.setStatus(200));

        HttpRequest req = new HttpRequest("GET", "/temp");
        HttpResponse res = new HttpResponse();

        chain.doFilter(req, res);

        assertThat(res.status()).isEqualTo(302);
        assertThat(res.headers()).containsEntry("Location", "https://example.com/temporary");
    }

    @Test
    void no_matching_rule_calls_next_in_chain() {
        RedirectFilter filter = new RedirectFilter(List.of(
                new RedirectRule(Pattern.compile("^/old-page$"), "/new-page", 301)
        ));

        AtomicBoolean terminalCalled = new AtomicBoolean(false);
        FilterChain chain = new FilterChain(new HttpFilter[] {filter}, (req, res) -> terminalCalled.set(true));

        HttpRequest req = new HttpRequest("GET", "/nope");
        HttpResponse res = new HttpResponse();

        chain.doFilter(req, res);

        assertThat(terminalCalled.get()).isTrue();
        assertThat(res.status()).isEqualTo(200);
        assertThat(res.headers()).doesNotContainKey("Location");
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

