package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Integration tests for IpFilter.
 * Verifies behavior in both ALLOWLIST and BLOCKLIST modes.
 */
class IpFilterTest {

    private IpFilter ipFilter;
    private HttpResponseBuilder response;
    private FilterChain mockChain;
    private boolean chainCalled;

    @BeforeEach
    void setUp() {
        ipFilter = new IpFilter();
        response = new HttpResponseBuilder();
        chainCalled = false;
        mockChain = (req, resp) -> chainCalled = true;
    }

    @Test
    void testBlocklistMode_AllowsUnblockedIp() {
        // ARRANGE
        ipFilter.setMode(IpFilter.FilterMode.BLOCKLIST);
        ipFilter.addBlockedIp("192.168.1.100");
        ipFilter.init();

        HttpRequest request = createRequestWithIp("192.168.1.50");

        // ACT
        ipFilter.doFilter(request, response, mockChain);

        // ASSERT
        assertThat(chainCalled).isTrue();
    }

    @Test
    void testBlocklistMode_BlocksBlockedIp() {
        // ARRANGE
        ipFilter.setMode(IpFilter.FilterMode.BLOCKLIST);
        ipFilter.addBlockedIp("192.168.1.100");
        ipFilter.init();

        HttpRequest request = createRequestWithIp("192.168.1.100");

        // ACT
        ipFilter.doFilter(request, response, mockChain);

        // ASSERT
        String result = new String(response.build(), StandardCharsets.UTF_8);
        assertAll(
                () -> assertThat(chainCalled).isFalse(),
                () -> assertThat(result).contains("403"),
                () -> assertThat(result).contains("Forbidden")
        );
    }

    @Test
    void testAllowListMode_AllowsWhitelistedIp() {
        // ARRANGE
        ipFilter.setMode(IpFilter.FilterMode.ALLOWLIST);
        ipFilter.addAllowedIp("10.0.0.1");
        ipFilter.init();

        HttpRequest request = createRequestWithIp("10.0.0.1");

        // ACT
        ipFilter.doFilter(request, response, mockChain);

        // ASSERT
        assertThat(chainCalled).isTrue();
    }

    @Test
    void testAllowListMode_BlockNonWhitelistedIp() {
        // ARRANGE
        ipFilter.setMode(IpFilter.FilterMode.ALLOWLIST);
        ipFilter.addAllowedIp("10.0.0.1");
        ipFilter.init();

        HttpRequest request = createRequestWithIp("10.0.0.2");

        // ACT
        ipFilter.doFilter(request, response, mockChain);

        // ASSERT
        assertThat(chainCalled).isFalse();

        String result = new String(response.build(), StandardCharsets.UTF_8);
        assertThat(result).contains("403");
    }

    @Test
    void testMissingClientIp_Returns400() {
        // ARRANGE
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Collections.emptyMap(),
                ""
        );

        // ACT
        ipFilter.doFilter(request, response, mockChain);

        // ASSERT
        String result = new String(response.build(), StandardCharsets.UTF_8);
        assertAll(
                () -> assertThat(chainCalled).isFalse(),
                () -> assertThat(result).contains("400"),
                () -> assertThat(result).contains("Bad Request")
        );
    }

    private HttpRequest createRequestWithIp(String ip) {
        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                Collections.emptyMap(),
                ""
        );
        request.setAttribute("clientIp", ip);
        return request;
    }

    // EDGE CASES

    @Test
    void testEmptyAllowlist_BlocksAllIps() {
        // ARRANGE
        ipFilter.setMode(IpFilter.FilterMode.ALLOWLIST);
        // Do not add Ip to the list

        // ACT
        HttpRequest request = createRequestWithIp("1.2.3.4");
        ipFilter.doFilter(request, response, mockChain);

        // ASSERT
        assertThat(chainCalled).isFalse();
    }

    @Test
    void testEmptyBlocklist_AllowAllIps() {
        // ARRANGE
        ipFilter.setMode(IpFilter.FilterMode.BLOCKLIST);
        // Do not add Ip to the list

        // ACT
        HttpRequest request = createRequestWithIp("1.2.3.4");
        ipFilter.doFilter(request, response, mockChain);

        // ASSERT
        assertThat(chainCalled).isTrue();
    }

    @Test
    void testEmptyStringIp() {
        // ARRANGE
        ipFilter.setMode(IpFilter.FilterMode.BLOCKLIST);
        HttpRequest request = createRequestWithIp("");

        // ACT
        ipFilter.doFilter(request, response, mockChain);

        // ASSERT
        String result = new String(response.build(), StandardCharsets.UTF_8);
        assertAll(
                () -> assertThat(chainCalled).isFalse(),
                () -> assertThat(result).contains("400"),
                () -> assertThat(result).contains("Bad Request")
        );
    }

    @Test
    void testAddBlockedIp_ThrowsOnNull() {
        assertThatThrownBy(() -> ipFilter.addBlockedIp(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void testAddAllowedIp_ThrowsOnNull() {
        assertThatThrownBy(() -> ipFilter.addAllowedIp(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

}
