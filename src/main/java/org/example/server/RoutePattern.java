package org.example.server;

public final class RoutePattern {

    private RoutePattern() {}

    public static boolean matches(String pattern, String path) {
        if (pattern == null || path == null) return false;

        if (pattern.endsWith("/*")) {
            String base = pattern.substring(0, pattern.length() - 2); // drop "/*"
            return path.equals(base) || path.startsWith(base + "/");
        }

        return pattern.equals(path);
    }
}