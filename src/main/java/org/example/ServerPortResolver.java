package org.example;

import org.example.config.AppConfig;
import org.example.config.ConfigLoader;

public final class ServerPortResolver {

    public static final int DEFAULT_PORT = 8080;
    private static final String PORT_FLAG = "--port";

    private ServerPortResolver() {
    }

    public static int resolvePort(String[] args) {
        Integer cliPort = parsePortFromCli(args);
        if (cliPort != null) {
            return validatePort(cliPort, "CLI argument " + PORT_FLAG);
        }

        Integer configPort = getPortFromLoadedConfigOrNull();
        if (configPort != null) {
            return validatePort(configPort, "configuration server.port");
        }

        return DEFAULT_PORT;
    }

    private static Integer getPortFromLoadedConfigOrNull() {
        AppConfig config = getLoadedConfigOrNull();
        if (config == null || config.server() == null) {
            return null;
        }
        return config.server().port();
    }

    private static AppConfig getLoadedConfigOrNull() {
        try {
            return ConfigLoader.get();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    static Integer parsePortFromCli(String[] args) {
        if (args == null) return null;

        for (int i = 0; i < args.length; i++) {
            if (PORT_FLAG.equals(args[i])) {
                int valueIndex = i + 1;
                if (valueIndex >= args.length) {
                    throw new IllegalArgumentException("Missing value after " + PORT_FLAG);
                }
                return parseIntOrThrow(args[valueIndex], "Invalid port value after " + PORT_FLAG);
            }
        }
        return null;
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
