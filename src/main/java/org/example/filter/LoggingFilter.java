package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void init() {
        //No initialization needed
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
        long startTime = System.nanoTime();

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            if(response.getStatusCode() == HttpResponseBuilder.SC_OK)
                response.setStatusCode(HttpResponseBuilder.SC_INTERNAL_SERVER_ERROR);
        } finally {
            long endTime = System.nanoTime();
            long processingTimeInMs = (endTime - startTime) / 1000000;

            log.info("REQUEST: {} {} | STATUS: {} | TIME: {}ms",
                    request.getMethod(), request.getPath(), response.getStatusCode(), processingTimeInMs);
        }

    }

    @Override
    public void destroy() {
        //No initialization needed
    }
}
