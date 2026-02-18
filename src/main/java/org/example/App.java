package org.example;

import org.example.config.AppConfig;
import org.example.config.ConfigLoader;

import java.nio.file.Path;

public class App {
    public static void main(String[] args) {
        Path configPath = Path.of("src/main/resources/application.yml");

        AppConfig appConfig = ConfigLoader.loadOnce(configPath);
        int port = appConfig.server().port();
        new TcpServer(port).start();
    }
}
