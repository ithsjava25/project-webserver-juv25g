package org.example;

import org.example.config.AppConfig;
import org.example.config.ConfigLoader;

import java.nio.file.Path;

public class App {
    public static void main(String[] args) {

        AppConfig appConfig = ConfigLoader.loadOnceWithClasspathFallback(
                Path.of("application.yml"),
                "application.yml"
        );

        int port = appConfig.server().port();
        new TcpServer(port).start();
    }
}
