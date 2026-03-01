package org.example;

import org.example.config.AppConfig;
import org.example.config.ConfigLoader;

import java.net.Socket;
import java.nio.file.Path;

public class App {

    private static final String PORT_FLAG = "--port";

    public static void main(String[] args) {

        AppConfig appConfig = ConfigLoader.loadOnceWithClasspathFallback(
                Path.of("application.yml"),
                "application.yml"
        );

        int port = resolvePort(args, appConfig.server().port());

        new TcpServer(port, ConnectionHandler::new).start();
    }

    static int resolvePort(String[] args, int configPort) {
        Integer cliPort = parsePortFromCli(args);
        if (cliPort != null) {
            return validatePort(cliPort, "CLI argument " + PORT_FLAG);
        }
        return validatePort(configPort, "configuration server.port");
    }

    static Integer parsePortFromCli(String[] args) {
        if (args == null) return null;

        for (int i = 0; i < args.length; i++) {
            if (PORT_FLAG.equals(args[i])) {
                int valueIndex = i + 1;
                if (valueIndex >= args.length) {
                    throw new IllegalArgumentException("Missing value after " + PORT_FLAG);
                }
                try {
                    return Integer.parseInt(args[valueIndex]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid port value after " + PORT_FLAG + ": " + args[valueIndex], e);
                }
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
}
