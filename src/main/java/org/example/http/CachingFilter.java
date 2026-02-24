package org.example.http;

import org.example.filter.Filter;
import org.example.filter.FilterChain;
import org.example.httpparser.HttpRequest;

import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
/// ///

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

        if (path.equals("/")) {
            path = "index.html";
        } else {
            path = path.substring(1);
        }

        File file = new File("www", path);

        if(!file.exists()){
            response.setStatusCode(404);
            return;
        }

        Map<String, String> headers = request.getHeaders();

        String modifiedSince = headers.get("If-Modified-Since");
        String eTag = generateEtag(file);
        Instant lastModified = Instant.ofEpochMilli(file.lastModified());

        String ifNoneMatch = headers.get("If-None-Match");


        if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
            response.setStatusCode(304);
            return;
        }


        if (modifiedSince != null) {
            try {
                Instant ifModifiedSinceInstant =
                        Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(modifiedSince));

                if (!lastModified.isAfter(ifModifiedSinceInstant)) {
                    response.setStatusCode(304);
                    return;
                }

            } catch (Exception e) {

            }
        }

        chain.doFilter(request, response);

        HttpCachingHeaders cachingHeaders = new HttpCachingHeaders();
        cachingHeaders.addETagHeader(eTag);
        cachingHeaders.setLastModified(Instant.ofEpochMilli(file.lastModified()));
        cachingHeaders.setDefaultCacheControlStatic();


        response.addHeader("ETag", eTag);

    }

    private String generateEtag(File file) {
        return String.valueOf(file.lastModified());

    }
}


