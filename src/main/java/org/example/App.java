package org.example;

import org.example.config.ConfigLoader;
import org.example.config.AppConfig;

import java.nio.file.Path;

public class App {
    public static void main(String[] args) {

        AppConfig config = ConfigLoader.loadOnce(Path.of("config.yml"));
        int port = config.server().port();

        new TcpServer(port).start();
    }
}

