package org.example.http;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpCachingHeadersTest {


    @Test
    void shouldStoreEtagValue() {

        HttpCachingHeaders cachingHeaders = new HttpCachingHeaders();

        cachingHeaders.addETagHeader("123456789");

        String etagValue = cachingHeaders.getHeaders().get("ETag");

        assertThat(etagValue).isEqualTo("123456789");

    }



    @Test
    void CacheControlReturnsCorrectValue() {
        HttpCachingHeaders cachingHeaders = new HttpCachingHeaders();
        cachingHeaders.setCacheControl("public, max-age=3600");

        String etagValue = cachingHeaders.getHeaders().get("Cache-Control");

        assertThat(etagValue).isEqualTo("public, max-age=3600");

    }

    @Test
    void setDefaultCacheControlStatic_shouldSetPublicMaxAge3600(){
        HttpCachingHeaders cachingHeaders = new HttpCachingHeaders();

        cachingHeaders.setDefaultCacheControlStatic();

        String etagValue = cachingHeaders.getHeaders().get("Cache-Control");

        assertThat(etagValue).isEqualTo("public, max-age=3600");

    }

    // Verifies that applyTo() copies all configured caching headers
    // into the provided target map.
    @Test
    void applyToResponse(){
        Map<String, String> target = new LinkedHashMap<>();
        HttpCachingHeaders cachingHeaders = new HttpCachingHeaders();
        cachingHeaders.addETagHeader("123456789");
        cachingHeaders.applyTo(target);

        assertThat(target).containsEntry("ETag", "123456789");
    }

    // Verifies that getHeaders() returns a defensive copy
    // so external modifications do not affect internal state.
    @Test
    void getHeaders_shouldReturnDefensiveCopy(){

        HttpCachingHeaders cachingHeaders = new HttpCachingHeaders();

        cachingHeaders.addETagHeader("123");

        Map<String, String> returnedMap = cachingHeaders.getHeaders();

        returnedMap.put("Etag", "hacked");

        Map<String, String> returnedMap2 = cachingHeaders.getHeaders();

        assertThat(returnedMap.get("Etag")).isEqualTo("hacked");

        assertThat(cachingHeaders.getHeaders().get("ETag")).isEqualTo("123");

    }
}
