package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.util.logging.Logger;

public class LoggFilter implements Filter{

    private static final Logger logger = Logger.getLogger(LoggFilter.class.getName());

    @Override
    public void init() {

    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
        long startTime = System.nanoTime();

        try{
            chain.doFilter(request, response);
        } finally {
            long endTime = System.nanoTime();
            double durationMs = (endTime -startTime) / 1_000_000.0;

            String message = String.format("%s %s | Status: %d | Time: %.3f ms",
                    request.getMethod(), request.getPath(), response.getStatusCode(), durationMs);

            logger.info(message);
        }

    }

    @Override
    public void destroy() {

    }
}
