package org.example.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void reset() {
        ConfigLoader.resetForTests();
    }

    @Test
    @DisplayName("Should return default configuration when config file is missing")
    void load_returns_defaults_when_file_missing() {
        Path missing = tempDir.resolve("missing.yml");

        AppConfig appConfig = ConfigLoader.load(missing).withDefaultsApplied();

        assertThat(appConfig.server().port()).isEqualTo(8080);
        assertThat(appConfig.server().rootDir()).isEqualTo("./www");
        assertThat(appConfig.logging().level()).isEqualTo("INFO");
    }

    @Test
    @DisplayName("Should load values from YAML file when file exists")
    void loadOnce_reads_yaml_values() throws Exception {
        Path configFile = tempDir.resolve("application.yml");
        Files.writeString(configFile, """
                server:
                  port: 9090
                  rootDir: ./public
                logging:
                  level: DEBUG
                """);

        AppConfig appConfig = ConfigLoader.loadOnce(configFile);

        assertThat(appConfig.server().port()).isEqualTo(9090);
        assertThat(appConfig.server().rootDir()).isEqualTo("./public");
        assertThat(appConfig.logging().level()).isEqualTo("DEBUG");
    }

    @Test
    @DisplayName("Should apply default values when sections or fields are missing")
    void defaults_applied_when_sections_or_fields_missing() throws Exception {
        Path configFile = tempDir.resolve("application.yml");
        Files.writeString(configFile, """
                server:
                  port: 1234
                """);

        AppConfig cfg = ConfigLoader.loadOnce(configFile);

        assertThat(cfg.server().port()).isEqualTo(1234);
        assertThat(cfg.server().rootDir()).isEqualTo("./www"); // default
        assertThat(cfg.logging().level()).isEqualTo("INFO");    // default
    }

    @Test
    @DisplayName("Should ignore unknown fields in configuration file")
    void unknown_fields_are_ignored() throws Exception {
        Path configFile = tempDir.resolve("application.yml");
        Files.writeString(configFile, """
                server:
                  port: 8081
                  rootDir: ./www
                  threads: 8
                logging:
                  level: INFO
                  json: true
                """);

        AppConfig cfg = ConfigLoader.loadOnce(configFile);

        assertThat(cfg.server().port()).isEqualTo(8081);
        assertThat(cfg.server().rootDir()).isEqualTo("./www");
        assertThat(cfg.logging().level()).isEqualTo("INFO");
    }

    @Test
    @DisplayName("Should return same instance on repeated loadOnce calls")
    void loadOnce_caches_same_instance() throws Exception {
        Path configFile = tempDir.resolve("application.yml");
        Files.writeString(configFile, """
                server:
                  port: 8080
                  rootDir: ./www
                logging:
                  level: INFO
                """);

        AppConfig a = ConfigLoader.loadOnce(configFile);
        AppConfig b = ConfigLoader.loadOnce(configFile);

        assertThat(a).isSameAs(b);
    }

    @Test
    @DisplayName("Should throw exception when get is called before configuration is loaded")
    void get_throws_if_not_loaded() {
        assertThatThrownBy(ConfigLoader::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not loaded");
    }

    @Test
    @DisplayName("Should fail fast when configuration file is invalid")
    void invalid_yaml_fails_fast() throws Exception {
        Path configFile = tempDir.resolve("broken.yml");
        Files.writeString(configFile, "server:\n  port 8080\n"); // saknar ':' efter port

        assertThatThrownBy(() -> ConfigLoader.load(configFile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("failed to read config file");
    }
}
