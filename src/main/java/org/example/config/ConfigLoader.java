package org.example.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
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

        // 2) väljer mapper baserat på filändelse (ska fungera med både JSON och YAML)
        ObjectMapper objectMapper = createMapperFor(configPath);

        // 3) läsning och parsning
        try (InputStream stream = Files.newInputStream(configPath)){
            Config config = objectMapper.readValue(stream, Config.class);
            return config.withDefaultsApplied();
        } catch (IOException e){
            throw new IllegalStateException("failed to read config file" + configPath.toAbsolutePath(), e);
        }
    }

    private static ObjectMapper createMapperFor(Path configPath) {
        String name = configPath.getFileName().toString().toLowerCase();

        ObjectMapper objectMapper;
        if (name.endsWith(".yml") || name.endsWith(".yaml")) {
            objectMapper = new ObjectMapper(new YAMLFactory());
        } else if (name.endsWith(".json")) {
            objectMapper = new ObjectMapper(); //vanlig json
        } else  {
            objectMapper = new ObjectMapper(new YAMLFactory()); //default: försöker med yaml
        }
        // - ignorera okända fält för att nya settings utan att krascha äldre builds
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
    // ======== config-modell ========

    public static final class Config {
        public ServerConfig server;
        public LoggingConfig logging;

        public static Config defaults() {
            Config config = new Config();
            config.server = ServerConfig.defaults();
            config.logging = LoggingConfig.defaults();
            return config;
        }

        /**
         * applicerar defaults på null-delar (om t.ex. logging-sektionen saknas i filen).
         */
        public Config withDefaultsApplied() {
            if (this.server == null) this.server = ServerConfig.defaults();
            else this.server.applyDefaults();

            if (this.logging == null) this.logging = LoggingConfig.defaults();
            else this.logging.applyDefaults();

            return this;
        }
    }

    // ======== config-modell ========
    // Static klasser som endast är till för ConfigLoader (inkapsling)
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
