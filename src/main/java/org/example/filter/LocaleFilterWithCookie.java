package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.util.Map;

/**
 * Filter that determines the preferred locale for an HTTP request using cookies and headers.
 * <p>
 * First, it checks for a locale set in a cookie named "user-lang". If the cookie is missing,
 * blank, or malformed, it falls back to the Accept-Language header. If neither is present
 * or valid, the filter defaults to "en-US".
 * <p>
 * The selected locale is stored in a ThreadLocal variable so it can be accessed throughout
 * the processing of the request.
 * <p>
 * This filter does not modify the response or stop the filter chain; it only sets the
 * current locale and forwards the request to the next filter.
 * <p>
 * ThreadLocal cleanup is performed after the filter chain completes to prevent memory leaks.
 */
public class LocaleFilterWithCookie implements Filter {

    private static final String DEFAULT_LOCALE = "en-US";
    private static final String LOCALE_COOKIE_NAME = "user-lang";
    private static final ThreadLocal<String> currentLocale = new ThreadLocal<>();

    @Override
    public void init() {
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
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

    private String resolveLocale(HttpRequest request) {
        String cookieLocale = extractLocaleFromCookie(request);
        if (cookieLocale != null && !cookieLocale.isBlank()) {
            return cookieLocale;
        }

        String headerLocale = extractLocaleFromHeader(request);
        if (headerLocale != null && !headerLocale.isBlank()) {
            return headerLocale;
        }

        return DEFAULT_LOCALE;
    }

    /**
     * Extracts the locale from the "user-lang" cookie if present.
     * <p>
     * If the cookie header is missing, blank, or malformed, returns null.
     */
    private String extractLocaleFromCookie(HttpRequest request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            return null;
        }

        String cookieHeader = headers.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase("Cookie"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2) {
                String name = parts[0].trim();
                String value = parts[1].trim();
                if (LOCALE_COOKIE_NAME.equals(name) && !value.isBlank()) {
                    return value;
                }
            }
        }

        return null;
    }

    /**
     * Extracts the preferred locale from the Accept-Language header of the request.
     * <p>
     * If the header is missing, blank, or malformed, returns null.
     * The first language tag is used and any optional quality value (e.g., ";q=0.9") is stripped.
     */
    private String extractLocaleFromHeader(HttpRequest request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            return null;
        }

        String acceptLanguage = headers.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase("Accept-Language"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return null;
        }

        String[] parts = acceptLanguage.split(",");
        if (parts.length == 0 || parts[0].isBlank()) {
            return null;
        }

        String locale = parts[0].split(";")[0].trim();
        return locale.isEmpty() ? null : locale;
    }
}
