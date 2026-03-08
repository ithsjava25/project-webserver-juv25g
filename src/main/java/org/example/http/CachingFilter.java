package org.example.http;


import org.example.config.AppConfig;
import org.example.config.ConfigLoader;
import org.example.filter.Filter;
import org.example.filter.FilterChain;
import org.example.httpparser.HttpRequest;

import java.io.File;
import java.io.ObjectInputFilter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;


public class CachingFilter implements Filter {


    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {


        HttpCachingHeaders cachingHeaders = new HttpCachingHeaders();

        String resolvedPath = request.getResolvedPath();
        File file = new File(resolvedPath);
        if (!file.isFile()) {
            chain.doFilter(request, response);
            return;
        }


        Map<String, String> headers = request.getHeaders();

        String modifiedSince = headers.get("If-Modified-Since");
        String eTag = generateEtag(file);
        String quotedEtag = "\"" + eTag + "\"";
        Instant lastModified = Instant.ofEpochMilli(file.lastModified());

        String ifNoneMatch = headers.get("If-None-Match");

        cachingHeaders.addETagHeader(eTag);
        cachingHeaders.setLastModified(Instant.ofEpochMilli(file.lastModified()));
        cachingHeaders.setDefaultCacheControlStatic();


        if (ifNoneMatch != null && ifNoneMatch.equals(quotedEtag)) {
            response.setStatusCode(HttpResponseBuilder.SC_NOT_MODIFIED);
            cachingHeaders.getHeaders().forEach(response::addHeader);
            return;
        }

        if (modifiedSince != null) {
            try {
                Instant ifModifiedSinceInstant =
                        Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(modifiedSince));

                if (!lastModified.isAfter(ifModifiedSinceInstant)) {
                    response.setStatusCode(HttpResponseBuilder.SC_NOT_MODIFIED);
                    cachingHeaders.getHeaders().forEach(response::addHeader);
                    return;
                }

            } catch (Exception e) {

                // Ignore malformed If-Modified-Since header; proceed without cache validation
                // Consider logging: log.debug("Invalid If-Modified-Since header: {}", modifiedSince, e);
            }

        }






        chain.doFilter(request, response);
        cachingHeaders.getHeaders().forEach(response::addHeader);


    }

    private String generateEtag(File file) {
        return file.lastModified() + "-" + file.length();

    }
}


