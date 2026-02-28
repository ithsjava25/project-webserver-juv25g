package org.example.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks how many requests come from each client locale.
 * <p>
 * Usage for debugging and inspection:
 * 1) Make sure LocaleFilterWithCookie and LocaleStatsFilter are integrated
 * in the ConnectionHandler filter chain.
 * 2) Set a breakpoint on the line:
 * localeCounts.merge(locale, 1, Integer::sum);
 * 3) Start the application in debug mode and open a browser to connect to
 * the server.
 * 4) When the breakpoint is hit, press Alt + F8 in IntelliJ to evaluate:
 * LocaleStatsFilter.getLocaleStats()
 * This shows the current counts of requests per locale.
 * 5) Change the browser's preferred language or Accept-Language header,
 * reload pages, and re-evaluate to see updated stats.
 * 6) Optionally, call LocaleStatsFilter.resetStatsForTests() to clear
 * the statistics and start fresh.
 * <p>
 * Thread-safety: the internal map is synchronized to allow safe updates
 * from multiple concurrent requests.
 */
public class LocaleStatsFilter implements Filter {

    private static final Map<String, Integer> localeCounts =
            Collections.synchronizedMap(new HashMap<>());

    @Override
    public void init() {
    }

    @Override
    public void doFilter(org.example.httpparser.HttpRequest request,
                         org.example.http.HttpResponseBuilder response,
                         FilterChain chain) {
        try {
            String locale = LocaleFilterWithCookie.getCurrentLocale();
            if (locale != null && !locale.isBlank()) {
                localeCounts.merge(locale, 1, Integer::sum);
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
        }
    }

    @Override
    public void destroy() {
    }

    public static Map<String, Integer> getLocaleStats() {
        synchronized (localeCounts) {
            return new HashMap<>(localeCounts);
        }
    }

    static void resetStatsForTests() {
        synchronized (localeCounts) {
            localeCounts.clear();
        }
    }
}
