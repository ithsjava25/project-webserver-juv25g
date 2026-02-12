package org.example.server;

import java.util.regex.Pattern;

public final class RedirectRulesLoader {
    private static final String REGEX_PREFIX = "regex:";

    private RedirectRulesLoader() {}

    public static Pattern compileSourcePattern(String sourcePath) {
        if (sourcePath == null || sourcePath.isBlank()) {
            throw new IllegalArgumentException("sourcePath must not be blank");
        }

        String trimmed = sourcePath.trim();

        if (trimmed.startsWith(REGEX_PREFIX)) {
            String rawRegex = trimmed.substring(REGEX_PREFIX.length());
            if (rawRegex.isBlank()) {
                throw new IllegalArgumentException("regex sourcePath must not be blank");
            }
            return Pattern.compile(rawRegex);
        }

        String regex;
        if (trimmed.contains("*")) {
            regex = wildcardToRegex(trimmed);
        } else {
            regex = Pattern.quote(trimmed);
        }
        return Pattern.compile("^" + regex + "$");
    }

    private static String wildcardToRegex(String wildcard) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wildcard.length(); i++) {
            char c = wildcard.charAt(i);
            if (c == '*') {
                sb.append("[^/]*");
            } else {
                sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return sb.toString();
    }
}

