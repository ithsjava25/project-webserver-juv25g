package org.example.server;

import java.util.Objects;
import java.util.regex.Pattern;

public final class RedirectRule {
    private final Pattern sourcePattern;
    private final String targetUrl;
    private final int statusCode;

    public RedirectRule(Pattern sourcePattern, String targetUrl, int statusCode) {
        this.sourcePattern = Objects.requireNonNull(sourcePattern, "sourcePattern");
        this.targetUrl = Objects.requireNonNull(targetUrl, "targetUrl");
        if (statusCode != 301 && statusCode != 302) {
            throw new IllegalArgumentException("statusCode must be 301 or 302");
        }
        this.statusCode = statusCode;
    }

    public Pattern getSourcePattern() { return sourcePattern; }
    public String getTargetUrl() { return targetUrl; }
    public int getStatusCode() { return statusCode; }

    public boolean matches(String requestPath) {
        return sourcePattern.matcher(requestPath).matches();
    }

    @Override
    public String toString() {
        return "RedirectRule{" +
                "sourcePattern=" + sourcePattern +
                ", targetUrl='" + targetUrl + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}

