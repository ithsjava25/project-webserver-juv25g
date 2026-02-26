package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.example.http.HttpResponseBuilder.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class RequestTimeOutFilterTest {

    private RequestTimeOutFilter filter;
    private HttpResponseBuilder response;
    private HttpRequest request;

    @BeforeEach
    void setUp() {
        filter = new RequestTimeOutFilter(100);
        response = new HttpResponseBuilder();
        request = new HttpRequest("GET", "/", "HTTP/1.1",null,"");
    }


    // Happy Path --> Allt går bra
    @Test
    void requestTimeOutFilter_shouldSucceedWhenFast() {

        // Arrange --> FilterChain som körs utan fördröjning
        FilterChain fastChain = (request, response) -> {};

        // Act
        filter.doFilter(request, response, fastChain);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(SC_OK);
    }

    // Timeout Path --> Anropet tar för lång tid och kastar RunTimeException
    @Test
    void requestTimeOutFilter_shouldThrowExceptionWhenSlow() {
        // Arrange --> En simulation av en fördröjning
        FilterChain slowChain = (request, response) -> {
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // Act
        filter.doFilter(request, response, slowChain);

        // Assert
        assertThat(response.getStatusCode())
                .as("Status code should be 504 at timeout")
                .isEqualTo(SC_GATEWAY_TIMEOUT);

        assertThat(new String(response.build()))
                .contains("Gateway Timeout");
    }

    // Exception Path --> Oväntat undantag kastar en exception
    @Test
    void requestTimeOutFilter_shouldHandleGenericException() {
        // Arrange
        FilterChain errorChain = (request, response) -> {
            throw new RuntimeException("Unexpected error");
        };

        // Act
        filter.doFilter(request, response, errorChain);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo((SC_INTERNAL_SERVER_ERROR));

    }
}
