package org.example.http;

import org.example.filter.Filter;
import org.example.filter.FilterChain;
import org.example.httpparser.HttpRequest;

import java.io.File;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
/// /

public class CachingFilter implements Filter {


    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {

        String path = request.getPath();

        File file = new File("www", path);

        if(!file.exists()){
            response.setStatusCode(404);
            return;
        }

        Map<String, String> headers = request.getHeaders();

        String modifiedSince = headers.get("If-Modified-Since");
        String ETag = generateEtag(file);
        Instant lastModified =
                Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(modifiedSince));

        String ifNoneMatch = headers.get("If-None-Match");


        if (ifNoneMatch != null && ifNoneMatch.equals(ETag)) {
            response.setStatusCode(304);
            return;
        }
        if (!lastModified.isAfter(Instant.parse(modifiedSince))) {
            response.setStatusCode(304);
            return;
        }

        chain.doFilter(request, response);

        HttpCachingHeaders cachingHeaders = new HttpCachingHeaders();
        cachingHeaders.addETagHeader(ETag);
        cachingHeaders.setLastModified(Instant.ofEpochMilli(file.lastModified()));
        cachingHeaders.setDefaultCacheControlStatic();

        response.setHeaders(cachingHeaders.getHeaders());

    }

    private String generateEtag(File file) {
        return String.valueOf(file.lastModified());

    }
}


