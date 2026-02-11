package org.example.server;

import java.util.regex.Pattern;

public final class RedirectRulesLoader {
    private RedirectRulesLoader() {}

    public static Pattern compileSourcePattern(String sourcePath) {
        if (sourcePath == null || sourcePath.isBlank()) {
            throw new IllegalArgumentException("sourcePath must not be blank");
        }

        String regex;
        if (sourcePath.contains("*")) {
            regex = wildcardToRegex(sourcePath);
        } else {
            regex = Pattern.quote(sourcePath);
        }
        return Pattern.compile("^" + regex + "$");
    }

    private static String wildcardToRegex(String wildcard) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wildcard.length(); i++) {
            char c = wildcard.charAt(i);
            if (c == '*') {
                sb.append(".*");
            } else {
                sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return sb.toString();
    }
}
