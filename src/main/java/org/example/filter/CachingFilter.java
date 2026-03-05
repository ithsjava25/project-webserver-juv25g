package org.example.filter;

import org.example.config.ConfigLoader;
import org.example.http.HttpCachingHeaders;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.io.File;
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

        String path = request.getPath();
        HttpCachingHeaders cachingHeaders = new HttpCachingHeaders();

        if (path.equals("/")) {
            path = "index.html";
        } else {
            path = path.substring(1);
        }

        // Ingen mer hårtkodat utan sökväg från ConfigLoader
        String rootDir = ConfigLoader.get().server().rootDir();
        File file = new File(rootDir, path);


        if(!file.exists()){
            response.setStatusCode(HttpResponseBuilder.SC_NOT_FOUND);

            return;
        }

        Map<String, String> headers = request.getHeaders();

        String modifiedSince = headers.get("If-Modified-Since");
        String eTag = generateEtag(file);
        Instant lastModified = Instant.ofEpochMilli(file.lastModified());

        String ifNoneMatch = headers.get("If-None-Match");

        cachingHeaders.addETagHeader(eTag);
        cachingHeaders.setLastModified(Instant.ofEpochMilli(file.lastModified()));
        cachingHeaders.setDefaultCacheControlStatic();


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

        chain.doFilter(request, response);
        cachingHeaders.getHeaders().forEach(response::addHeader);


    }

    private String generateEtag(File file) {
        return "\"" + file.lastModified() + "-" + file.length() + "\"";

    }
}

