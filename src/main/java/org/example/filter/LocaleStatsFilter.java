package org.example.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
            e.printStackTrace();
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
