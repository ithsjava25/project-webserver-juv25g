package org.example.http;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for building HTTP response headers
 * Lets the client reuse cached responses
 * Reduces bandwidth
 * Reduces latency
 * Reduces load on your server
 * Ensures webserver and proxies understand caching instructions
 */
public class HttpCachingHeaders {

    /**
     *  Cache Control helps manage servers and browsers by settings rules
     *  ETag helps cache be more efficient and not needing to send a full resend assuming the content has not changed
     *  Last-Modified
     */
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String LAST_MODIFIED = "Last-Modified";
    private static final String ETAG = "ETag";

    private static final DateTimeFormatter HTTP_DATE_FORMATTER =
            DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);


    private final Map<String, String> headers = new LinkedHashMap<>();


    /**
     * Sets a header
     * @param name Header name eg. Cache-Control
     * @param value Header value eg. public, max-age=3600
     */
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * Helper method for setting ETag header value
     * ETag values must be enclosed in double quotes "123" not 123
     * @param etag ETag value as quouted per RFC 7232
     *
     */
    public void addETagHeader(String etag) {
        if (etag == null) return;

        String formattedEtag;
        if (etag.startsWith("\"") && etag.endsWith("W/\"") ) {
            formattedEtag = etag;
        } else {
            formattedEtag = "\"" + etag + "\"";
        }
            setHeader(ETAG, etag);


    }

    /**
     * Sets Cache-Control header value
     * @param cacheControl sets rules eg. public, max-age=3600
     */
    public void setCacheControl(String cacheControl) {
        setHeader(CACHE_CONTROL, cacheControl);
    }

    /**
     * Helper method for setting Last-Modified header value
     * Formates and sets Last modified based on an instant
     * @param instant Timestamp of the last modification
     */
    public void setLastModified(Instant instant){
        setHeader(LAST_MODIFIED, HTTP_DATE_FORMATTER.format(instant));
    }


    /**
     * In case of errors or unexpected behaviour, cache should be disabled and no data should be saved
     */
    public void setNoCache() {
        setCacheControl("no-store, no-cache, must-revalidate");
    }


    /**
     * Copies all configured caching headers into the provided target map eg. HttpReponseBuilder.
     * @param target Map should return generated headers
     */
    public void applyTo(Map<String,String> target){
        target.putAll(headers);
    }

    /**
     * Maps all configured caching headers into a new map
     * @return A map which includes all caching headers
     */
    public Map<String,String> getHeaders() {
        return new LinkedHashMap<>(headers);
    }

    /**
     * Standard settings for caching, 1 hour
     */
    public void setDefaultCacheControlStatic(){
        setCacheControl("public, max-age=3600");
    }



}
