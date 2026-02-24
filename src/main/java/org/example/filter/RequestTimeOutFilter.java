package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import static org.example.http.HttpResponseBuilder.*;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class RequestTimeOutFilter implements Filter {

    private final int timeoutMS;
    private static final Logger logger = Logger.getLogger(RequestTimeOutFilter.class.getName());

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public RequestTimeOutFilter(int timeoutMS) {
        this.timeoutMS = timeoutMS;
    }

    @Override
    public void init() {}

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {


        Future<?> future = executor.submit(() -> {
            chain.doFilter(request, response);
        });

        try {
            future.get(timeoutMS, TimeUnit.MILLISECONDS);

        } catch (TimeoutException e) {
            future.cancel(true);

            logger.warning("TIMEOUT PROAKTIV: " + request.getPath() + " avbröts efter " + timeoutMS + "ms");


            response.setStatusCode(SC_GATEWAY_TIMEOUT);
            response.setHeaders(Map.of("Content-Type", "text/html; charset=utf-8"));
            response.setBody("<h1>504 Gateway Timeout</h1><p>Processen avbröts eftersom den tog för lång tid.</p>");


            throw new RuntimeException("Timeout reached in filter");

        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Fel under exekvering: " + e.getMessage());
            response.setStatusCode(SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException("Filter execution error", e);
        }
    }

    @Override
    public void destroy() {

    }
}
