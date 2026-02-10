package org.example.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * configloader:
 * - försöker läsa config från fil (yaml/json)
 * - om filen inte finns: returnerar defaults
 * - exponerar en "loadOnce"-cache så andra delar av programmet kan använda configen
 */
public final class ConfigLoader {

    // Cachear configen efter första lyckade laddningen
    private static volatile Config cached;

    private ConfigLoader() {}

    /**
     * laddar config en gång och cachear resultatet.
     * anropa t.ex. vid uppstart (main) och använd ConfigLoader.get() i resten av appen.
     */
    public static Config loadOnce(Path configPath) {
        if (cached != null) return cached;

        synchronized (ConfigLoader.class) {
            if (cached != null) return cached;

            cached = load(configPath);
            return cached;
        }
    }
    /**
     * försöker läsa config från filsystemet. om den inte finns -> defaults.
     */
    public static Config load(Path configPath) {
        Objects.requireNonNull(configPath, "configPath");

        // 1) om config inte finns: defaults
        if (!Files.exists(configPath)) {
            return Config.defaults();
        }
        return Config.defaults();
    }

    // ======== config-modell ========
    // Static klasser som endast är till för ConfigLoader (inkapsling)

    public static final class Config {
        public ServerConfig server;
        public LoggingConfig logging;

        public static Config defaults() {
            Config config = new Config();
            config.server = ServerConfig.defaults();
            config.logging = LoggingConfig.defaults();
            return config;
        }
    }

    public static final class ServerConfig {
        public Integer port;     // Integer så vi kan upptäcka "saknas" (null) och sätta default
        public String rootDir;

        public static ServerConfig defaults() {
            ServerConfig serverConfig = new ServerConfig();
            serverConfig.port = 8080;
            serverConfig.rootDir = "./www";
            return serverConfig;
        }

        public void applyDefaults() {
            if (this.port == null) this.port = 8080;
            if (this.rootDir == null || this.rootDir.isBlank()) this.rootDir = "./www";
        }
    }

    public static final class LoggingConfig {
        public String level;

        public static LoggingConfig defaults() {
            LoggingConfig loggingConfig = new LoggingConfig();
            loggingConfig.level = "INFO";
            return loggingConfig;
        }

        public void applyDefaults() {
            if (this.level == null || this.level.isBlank()) this.level = "INFO";
        }
    }
}
