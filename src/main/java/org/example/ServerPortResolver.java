package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ServerPortResolver {

    public static final int DEFAULT_PORT = 8080;

    private ServerPortResolver() {
    }

    public static int resolvePort(String[] args) {
        Integer cliPort = readPortFromCli(args);
        if (cliPort != null) {
            return validatePort(cliPort, "CLI argument --port");
        }

        Integer configPort = readPortFromClasspathProperties("ConnectionConfig.properties", "port");
        if (configPort != null) {
            return validatePort(configPort, "config file ConnectionConfig.properties");
        }

        return DEFAULT_PORT;
    }

    static Integer readPortFromCli(String[] args) {
        if (args == null) return null;

        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i])) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value after --port");
                }
                return parseIntOrThrow(args[i + 1], "Invalid port value after --port");
            }
        }
        return null;
    }

    static Integer readPortFromClasspathProperties(String resourceName, String key) {
        Properties props = new Properties();

        try (InputStream in = ServerPortResolver.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) return null;

            props.load(in);
            String value = props.getProperty(key);
            if (value == null || value.isBlank()) return null;

            return parseIntOrThrow(value.trim(), "Invalid integer for " + key + " in " + resourceName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + resourceName, e);
        }
    }

    static int validatePort(int port, String source) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port out of range (1-65535) from " + source + ": " + port);
        }
        return port;
    }

    static int parseIntOrThrow(String s, String message) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message + ": " + s, e);
        }
    }
}
