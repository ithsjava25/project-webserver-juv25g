package org.example.server;

import java.util.regex.Pattern;

public final class RedirectRule {
    private final Pattern sourcePattern;
    private final String targetUrl;
    private final int statusCode;

    public RedirectRule(Pattern sourcePattern, String targetUrl, int statusCode) {
        this.sourcePattern = sourcePattern;
        this.targetUrl = targetUrl;
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
