package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.util.Map;

/**
 * Filter that extracts the preferred locale from the Accept-Language header of an HTTP request.
 * <p>
 * If the Accept-Language header is missing, blank, or malformed, the filter defaults to "en-US".
 * The selected locale is stored in a ThreadLocal variable so it can be accessed during the request.
 * <p>
 * This filter does not modify the response or stop the filter chain; it simply sets the
 * current locale and forwards the request to the next filter in the chain.
 * <p>
 * ThreadLocal cleanup is performed after the filter chain completes to prevent memory leaks.
 */
public class LocaleFilter implements Filter {

    private static final String DEFAULT_LOCALE = "en-US";
    private static final ThreadLocal<String> currentLocale = new ThreadLocal<>();

    @Override
    public void init() {
    }

    @Override
    public void doFilter(HttpRequest request,
                         HttpResponseBuilder response,
                         FilterChain chain) {
        try {
            String locale = resolveLocale(request);
            currentLocale.set(locale);

            chain.doFilter(request, response);
        } finally {
            currentLocale.remove();
        }
    }

    @Override
    public void destroy() {
    }

    public static String getCurrentLocale() {
        String locale = currentLocale.get();
        if (locale != null) {
            return locale;
        } else {
            return DEFAULT_LOCALE;
        }
    }

    /**
     * Determines the preferred locale from the Accept-Language header of the request.
     * If the header is missing, blank, or malformed, this method returns the default locale "en-US".
     * The first language tag is used, and any optional quality value (e.g., ";q=0.9") is stripped.
     * If the request itself is null, the default locale is also returned.
     */
    private String resolveLocale(HttpRequest request) {

        if (request == null) {
            return DEFAULT_LOCALE;
        }

        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return DEFAULT_LOCALE;
        }

        String acceptLanguage = null;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null &&
                    entry.getKey().equalsIgnoreCase("Accept-Language")) {
                acceptLanguage = entry.getValue();
                break;
            }
        }

        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return DEFAULT_LOCALE;
        }

        String[] parts = acceptLanguage.split(",");
        if (parts[0].isBlank()) {
            return DEFAULT_LOCALE;
        }

        String locale = parts[0].split(";")[0].trim();
        if (locale.isEmpty()) {
            return DEFAULT_LOCALE;
        } else {
            return locale;
        }
    }
}
