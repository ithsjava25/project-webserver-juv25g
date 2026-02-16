package org.example.filter;

import org.example.http.HttpResponse;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class IpFilterTest {

    private IpFilter ipFilter;
    private HttpResponse response;
    private FilterChain mockChain;
    private boolean chainCalled;

    @BeforeEach
    void setUp() {
        ipFilter = new IpFilter();
        response = new HttpResponse();
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
        String result = response.build();
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

        String result = response.build();
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
        String result = response.build();
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

}
