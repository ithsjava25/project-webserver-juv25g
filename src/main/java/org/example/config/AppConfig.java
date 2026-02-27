package org.example.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppConfig(
        @JsonProperty("server") ServerConfig server,
        @JsonProperty("logging") LoggingConfig logging,
        @JsonProperty("ipFilter") IpFilterConfig ipFilter,
        @JsonProperty("maxRequestBody") MaxRequestBodyConfig maxRequestBody

) {
    public static AppConfig defaults() {
        return new AppConfig(
                ServerConfig.defaults(),
                LoggingConfig.defaults(),
                IpFilterConfig.defaults(),
                MaxRequestBodyConfig.defaults()
        );
    }

    public AppConfig withDefaultsApplied() {
        ServerConfig serverConfig = (server == null ? ServerConfig.defaults() : server.withDefaultsApplied());
        LoggingConfig loggingConfig = (logging == null ? LoggingConfig.defaults() : logging.withDefaultsApplied());
        IpFilterConfig ipFilterConfig = (ipFilter == null ? IpFilterConfig.defaults() : ipFilter.withDefaultsApplied());
        MaxRequestBodyConfig maxRequestBodyConfig =
                (maxRequestBody == null ? MaxRequestBodyConfig.defaults() : maxRequestBody.withDefaultsApplied());

        return new AppConfig(serverConfig, loggingConfig, ipFilterConfig, maxRequestBodyConfig);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IpFilterConfig(
            @JsonProperty("enabled") Boolean enabled,
            @JsonProperty("mode") String mode,
            @JsonProperty("blockedIps") java.util.List<String> blockedIps,
            @JsonProperty("allowedIps") java.util.List<String> allowedIps
    ) {
        public static IpFilterConfig defaults() {
            return new IpFilterConfig(false, "BLOCKLIST", java.util.List.of(), java.util.List.of());
        }

        public IpFilterConfig withDefaultsApplied() {
            Boolean e = (enabled == null) ? false : enabled;
            String m = (mode == null || mode.isBlank()) ? "BLOCKLIST" : mode;
            java.util.List<String> blocked = (blockedIps == null) ? java.util.List.of() : blockedIps;
            java.util.List<String> allowed = (allowedIps == null) ? java.util.List.of() : allowedIps;
            return new IpFilterConfig(e, m, blocked, allowed);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MaxRequestBodyConfig(
            @JsonProperty("enabled") Boolean enabled,
            @JsonProperty("maxBytes") Long maxBytes
    ) {
        public static MaxRequestBodyConfig defaults() {
            // välj en rimlig default för skolprojekt: 1 MiB
            return new MaxRequestBodyConfig(false, 1_048_576L);
        }

        public MaxRequestBodyConfig withDefaultsApplied() {
            Boolean enabledFlag = (enabled == null) ? false : enabled;
            long limit = (maxBytes == null) ? 1_048_576L : maxBytes;

            if (limit < 0) {
                throw new IllegalArgumentException("maxRequestBody.maxBytes must be >= 0");
            }

            return new MaxRequestBodyConfig(enabledFlag, limit);
        }
    }
}
