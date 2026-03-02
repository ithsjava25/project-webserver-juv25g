package org.example.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks how many requests come from each client locale (normalized and lowercased).
 * Only the first 256 unique locales are tracked; additional unknown locales are ignored.
 *
 * <p><strong>Important: Filter ordering requirement</strong></p>
 * This filter depends on LocaleFilterWithCookie having already executed
 * earlier in the filter chain. It retrieves the current locale via
 * {@code LocaleFilterWithCookie.getCurrentLocale()}, which internally
 * uses a ThreadLocal value set during that filter's execution.
 *
 * <p>
 * If LocaleFilterWithCookie is not executed before this filter,
 * the ThreadLocal will not be populated for the current request,
 * and the default locale ("en-US") will be returned instead.
 * Therefore, LocaleFilterWithCookie <strong>must</strong> be registered
 * before LocaleStatsFilter in the filter chain to ensure correct behavior.
 *
 * <p>
 * Usage for debugging and inspection:
 * <p>
 * 1) Make sure LocaleFilterWithCookie and LocaleStatsFilter are integrated
 * in the ConnectionHandler filter chain, with LocaleFilterWithCookie
 * placed before LocaleStatsFilter.
 * <p>
 * 2) Set a breakpoint on the line:
 * localeCounts.merge(normalized, 1, Integer::sum);
 * <p>
 * 3) Start the application in debug mode and open a browser to connect to
 * the server.
 * <p>
 * 4) When the breakpoint is hit, press Alt + F8 in IntelliJ to evaluate:
 * LocaleStatsFilter.getLocaleStats()
 * This shows the current counts of requests per locale.
 * <p>
 * 5) Change the browser's preferred language or Accept-Language header,
 * reload pages, and re-evaluate to see updated stats.
 * <p>
 * 6) Optionally, call LocaleStatsFilter.resetStatsForTests() to clear
 * the statistics and start fresh.
 *
 * <p>
 * Thread-safety: the internal map is synchronized to allow safe updates
 * from multiple concurrent requests.
 */
public class LocaleStatsFilter implements Filter {

    private static final int MAX_TRACKED_LOCALES = 256;

    private static final Map<String, Integer> localeCounts =
            Collections.synchronizedMap(new HashMap<>());

    @Override
    public void init() {
    }

    @Override
    public void doFilter(org.example.httpparser.HttpRequest request,
                         org.example.http.HttpResponseBuilder response,
                         FilterChain chain) {

        String locale = LocaleFilterWithCookie.getCurrentLocale();
        if (locale != null && !locale.isBlank()) {
            String normalized = locale.trim().toLowerCase();

            synchronized (localeCounts) {
                if (localeCounts.containsKey(normalized) || localeCounts.size() < MAX_TRACKED_LOCALES) {
                    localeCounts.merge(normalized, 1, Integer::sum);
                }
            }
        }

        chain.doFilter(request, response);
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
