package org.example.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppConfig(
        @JsonProperty("server") ServerConfig server,
        @JsonProperty("logging") LoggingConfig logging
) {
    public static AppConfig defaults() {
        return new AppConfig(ServerConfig.defaults(), LoggingConfig.defaults());
    }

    public AppConfig withDefaultsApplied() {
        ServerConfig serverConfig = (server == null ? ServerConfig.defaults() : server.withDefaultsApplied());
        LoggingConfig loggingConfig = (logging == null ? LoggingConfig.defaults() : logging.withDefaultsApplied());
        return new AppConfig(serverConfig, loggingConfig);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ServerConfig(
            @JsonProperty("port") Integer port,
            @JsonProperty("rootDir") String rootDir
    ) {
        public static ServerConfig defaults() {
            return new ServerConfig(8080, "./www");
        }

        public ServerConfig withDefaultsApplied() {
            int p = (port == null ? 8080 : port);
            if (p < 1 || p > 65535) {
                throw new IllegalArgumentException("Invalid port number: " + p + ". Port must be between 1 and 65535");
            }
            String rd = (rootDir == null || rootDir.isBlank()) ? "./www" : rootDir;
            return new ServerConfig(p, rd);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LoggingConfig(
            @JsonProperty("level") String level
    ) {
        public static LoggingConfig defaults() {
            return new LoggingConfig("INFO");
        }

        public LoggingConfig withDefaultsApplied() {
            String lvl = (level == null || level.isBlank()) ? "INFO" : level;
            return new LoggingConfig(lvl);
        }
    }
}
