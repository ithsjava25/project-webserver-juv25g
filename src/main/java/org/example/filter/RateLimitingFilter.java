package org.example.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Rate Limiting Filter responsible for limiting the number of requests per client IP.
 * Implements the Token Bucket algorithm using the Bucket4j library.
 * How it works:
 * A "bucket" hold a fixed number of tokens (capacity)
 * Each incoming request attempts to consume exactly one token
 * If a token is available, the request is processed and the token is removed
 * If the bucket is empty, the request is rejected with an HTTP 429 (Too Many Requests) status
 * Tokens are replenished at a fixed rate over time (Refill Rate), up to the maximum capaci
 * This allows for occasional bursts of traffic while maintaining a steady long-term rate limit
 * The capacity of the bucket is 10, and it refills one token per 10 seconds
 */

public class RateLimitingFilter implements Filter {
    private static final Logger logger = Logger.getLogger(RateLimitingFilter.class.getName());
    private static final Map<String, BucketWrapper> buckets = new ConcurrentHashMap<>();
    private static final long CAPACITY = 10;
    private static final long REFILL_TOKENS = 1;
    private final Duration refillPeriod = Duration.ofSeconds(10);
    private static final int MAX_BUCKETS_THRESHOLD = 1000;
    private final AtomicBoolean cleanupStarted = new AtomicBoolean(false);
    private volatile Thread cleanupThread;

    @Override
    public void init() {
        logger.info("RateLimitingFilter initialized with capacity: " + CAPACITY);
        if (cleanupStarted.compareAndSet(false, true)) {
            cleanupThread = startCleanupThread();
        }
    }

    /**
     * Intercepts the request and checks if the client has enough tokens.
     */
    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {

        String clientIp = resolveClientIp(request, response);

        if (clientIp == null) return;

        BucketWrapper wrapper = buckets.computeIfAbsent(clientIp, k -> new BucketWrapper(createNewBucket()));

        wrapper.updateAccess();

        if (wrapper.bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            logger.warning("Limit exceeded per IP: " + clientIp);
            response.setStatusCode(HttpResponseBuilder.SC_TOO_MANY_REQUESTS);
            response.setBody("<h1>429 Too Many Requests</h1><p> Limit of requests exceeded.</p>\n");
        }
    }

    @Override
    public void destroy() {
        Thread t = cleanupThread;
        if (t != null) {
            t.interrupt();
            cleanupThread = null;
        }
        cleanupStarted.set(false);
        buckets.clear();
    }

    /**
     * Configures a new Bucket with the specified bandwidth.
     */
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(CAPACITY)
                        .refillGreedy(REFILL_TOKENS, refillPeriod)
                        .build())
                .build();
    }

    /**
     * Track the last access time of every bucket
     */
    private static class BucketWrapper {
        private final Bucket bucket;
        private volatile long lastAccessTime;

        BucketWrapper(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccessTime = System.currentTimeMillis();
        }

        void updateAccess() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    public static String resolveClientIp(HttpRequest request, HttpResponseBuilder response) {

        Object clientIpAttr = request.getAttribute("clientIp");

        if (!(clientIpAttr instanceof String clientIp) || (clientIp.isBlank())) {
            response.setStatusCode(HttpResponseBuilder.SC_BAD_REQUEST);
            response.setBody("<h1>400 Bad Request</h1><p>Missing client IP.</p>\n");
            return null;
        }

        String xForwardedFor = request.getHeaders().get("X-Forwarded-For");

        if( xForwardedFor != null && !xForwardedFor.isBlank() ) {
            clientIp = xForwardedFor.split(",")[0].trim();
        }

        return clientIp;
    }

    public Thread startCleanupThread() {
        return Thread.ofVirtual().name("rate-limit-cleanup").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    //it checks every 10 minutes
                    Thread.sleep(Duration.ofMinutes(10).toMillis());

                    cleanupIdleBuckets();

                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void cleanupIdleBuckets() {
        //it will only clean when the size of the buckets is more than 1000
        if (buckets.size() > MAX_BUCKETS_THRESHOLD) {
            long idleThreshold = System.currentTimeMillis() - Duration.ofMinutes(30).toMillis();
            buckets.entrySet().removeIf(entry -> entry.getValue().lastAccessTime < idleThreshold);
        }
    }

    public int getBucketsCount() {
        return buckets.size();
    }

    public void ageBucketsForTesting(long millisToSubtract) {
        for (BucketWrapper wrapper : buckets.values()) {
            long oldTime = wrapper.lastAccessTime;
            wrapper.lastAccessTime = oldTime - millisToSubtract;
        }
    }

}
