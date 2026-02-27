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
   private final ExecutorService executor = new ThreadPoolExecutor(
           Math.max(4, Runtime.getRuntime().availableProcessors() * 2),
            Math.max(4, Runtime.getRuntime().availableProcessors() * 2),
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(50),
            new ThreadPoolExecutor.AbortPolicy()
    );

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
        // Preserve state already present on the real response before downstream execution
        transferResponseData(response, shadowResponse);

        Future<?> future;
             try {
                 future = executor.submit(() -> {
                     try {
                         chain.doFilter(request, shadowResponse);
                     } catch (Exception e) {
                         throw new RuntimeException(e);
                     }
                 });

             } catch (RejectedExecutionException e) {
                 logger.severe("SERVER OVERLOADED: Queue is full for path " + request.getPath());
                 response.setStatusCode(SC_SERVICE_UNAVAILABLE);
                 response.setHeaders(Map.of("Content-Type", "text/html; charset=utf-8"));
                 response.setBody("<h1>503 Service Unavailable</h1><p>Server is too busy to handle the request.</p>");
                 return;
             }

        try {
            future.get(timeoutMS, TimeUnit.MILLISECONDS);
            transferResponseData(shadowResponse, response);

        } catch (TimeoutException e) {
            future.cancel(true);
            logger.warning("TIMEOUT ERROR: " + request.getPath() + " was interrupted after " + timeoutMS + "ms");

                response.setStatusCode(SC_GATEWAY_TIMEOUT);
                response.setHeaders(Map.of("Content-Type", "text/html; charset=utf-8"));
                response.setBody("<h1>504 Gateway Timeout</h1><p>The server took too long to respond.</p>");

                return;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            handleInternalError(response, e);
            return;

        }  catch (ExecutionException e) {
            handleInternalError(response, e);
            return;
        }
    }
    private void transferResponseData(HttpResponseBuilder source, HttpResponseBuilder target) {
        target.setStatusCode(source.getStatusCode());
        target.setHeaders(source.getHeaders());

        byte[] sourceBytes = source.getByteBody();
        if (sourceBytes != null) {
            target.setBody(sourceBytes);
        } else {
            target.setBody(source.getBody());
        }
    }

    private void handleInternalError(HttpResponseBuilder response, Exception e) {
        logger.severe("Error during execution: " + e.getMessage());
        response.setStatusCode(SC_INTERNAL_SERVER_ERROR);
        response.setHeaders(Map.of("Content-Type", "text/html; charset=utf-8"));
        response.setBody("<h1>500 Internal Server Error</h1>");
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
