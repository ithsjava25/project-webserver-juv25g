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

        String rootDir = request.getResolvedPath();
        String path = request.getResolvedPath();
        File file = new File(rootDir, path);



        Map<String, String> headers = request.getHeaders();

        String modifiedSince = headers.get("If-Modified-Since");
        String eTag = generateEtag(file);
        Instant lastModified = Instant.ofEpochMilli(file.lastModified());

        String ifNoneMatch = headers.get("If-None-Match");


        if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
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

            }
        }




        cachingHeaders.addETagHeader(eTag);
        cachingHeaders.setLastModified(Instant.ofEpochMilli(file.lastModified()));
        cachingHeaders.setDefaultCacheControlStatic();

        chain.doFilter(request, response);
        cachingHeaders.getHeaders().forEach(response::addHeader);


    }

    private String generateEtag(File file) {
        return "\"" + file.lastModified() + "-" + file.length() + "\"";

    }
}


