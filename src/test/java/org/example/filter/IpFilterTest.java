package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
