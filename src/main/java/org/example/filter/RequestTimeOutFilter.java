package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import static org.example.http.HttpResponseBuilder.*;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * A proactive filter that monitors the execution time of the request processing chain.
 * If the execution exceeds the specified timeout, the filter interrupts the
 * processing thread and returns an HTTP 504 Gateway Timeout response.
 */
public class RequestTimeOutFilter implements Filter {

    private final int timeoutMS;
    private static final Logger logger = Logger.getLogger(RequestTimeOutFilter.class.getName());

    /** Thread pool used to execute the filter chain asynchronously for timeout monitoring. */
    private final ExecutorService executor = Executors.newFixedThreadPool(200);

    public RequestTimeOutFilter(int timeoutMS) {

        if (timeoutMS <= 0) {
            throw new IllegalArgumentException("timeoutMS must be greater than 0");
        }
        this.timeoutMS = timeoutMS;
    }

    @Override
    public void init() {}

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {

        HttpResponseBuilder shadowResponse = new HttpResponseBuilder();

        Future<?> future = executor.submit(() -> {
            try {
                chain.doFilter(request, shadowResponse);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
        });

        try {
            future.get(timeoutMS, TimeUnit.MILLISECONDS);

            transferResponseData(shadowResponse, response);

        } catch (TimeoutException e) {
            future.cancel(true);

            logger.warning("TIMEOUT ERROR: " + request.getPath() + " was interrupted after " + timeoutMS + "ms");

                response.setStatusCode(SC_GATEWAY_TIMEOUT);
                response.setHeaders(Map.of("Content-Type", "text/html; charset=utf-8"));
                response.setBody("<h1>504 Gateway Timeout</h1><p>The server took to long to respond.</p>");

            throw new RuntimeException("Timeout reached in filter");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Error during execution: " + e.getMessage());
            response.setStatusCode(SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException("Filter execution error", e);

        }  catch (ExecutionException e) {
            logger.severe("Error during execution: " + e.getMessage());
            response.setStatusCode(SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException("Filter execution error", e);
        }
    }
    private void transferResponseData(HttpResponseBuilder source, HttpResponseBuilder target) {

        target.setStatusCode(source.getStatusCode());
        target.setHeaders(source.getHeaders());

        if (source.getByteBody() != null) {
            target.setBody(source.getByteBody());
        } else {
            target.setBody(source.getBody());
        }
    }

    @Override
    public void destroy() {
        executor.shutdown();
        try {
            if(!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
