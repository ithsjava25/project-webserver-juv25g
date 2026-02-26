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

        String path = request.getPath();

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


        if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
            response.setStatusCode(HttpResponseBuilder.SC_NOT_MODIFIED);
            return;
        }


        if (modifiedSince != null) {
            try {
                Instant ifModifiedSinceInstant =
                        Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(modifiedSince));

                if (!lastModified.isAfter(ifModifiedSinceInstant)) {
                    response.setStatusCode(HttpResponseBuilder.SC_NOT_MODIFIED);
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
        response.addHeader("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME.format(lastModified.atZone(java.time.ZoneOffset.UTC)));
        response.addHeader("Cache-Control", "public, max-age=3600");

    }

    private String generateEtag(File file) {
        return "\"" + file.lastModified() + "-" + file.length() + "\"";

    }
}


