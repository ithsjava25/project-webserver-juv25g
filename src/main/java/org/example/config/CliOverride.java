package org.example.config;

public record CliOverride(
        Integer port,
        String rootDir,
        String logLevel
) {

    public static CliOverride empty() {
        return new CliOverride(null, null, null);
    }

    public boolean isEmpty() {
        return port == null && rootDir == null && logLevel == null;
    }
}
