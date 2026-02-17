package org.example.http;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpCachingHeaders {

    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String LAST_MODIFIED = "Last-Modified";
    private static final String ETAG = "ETag";

    private static final DateTimeFormatter HTTP_DATE_FORMATTER =
            DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);


    private final Map<String, String> headers = new LinkedHashMap<>();


    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addETagHeader(String etag) {
        setHeader(ETAG, etag);
    }

    public void setCacheControl(String cacheControl) {
        setHeader(CACHE_CONTROL, cacheControl);
    }

    public void setLastModified(Instant instant){
        setHeader(LAST_MODIFIED, HTTP_DATE_FORMATTER.format(instant));
    }

    public void setNoCache() {
        setCacheControl("no-store, no-cache, must-revalidate");
    }



    public void applyTo(Map<String,String> target){
        target.putAll(headers);
    }

    public Map<String,String> getHeaders() {
        return new LinkedHashMap<>(headers);
    }

    public void setDefaultCacheControlStatic(){
        setCacheControl("public, max-age=3600");
    }



}
