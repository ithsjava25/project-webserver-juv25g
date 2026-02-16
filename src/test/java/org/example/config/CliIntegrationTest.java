package org.example.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

public class CliIntegrationTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void resetCache() {
        ConfigLoader.resetForTests();
    }

    @Test
    @DisplayName("cli inputs should override values in config file")
    void cli_overrides_config_file_values() throws IOException {
        Path configFile = tempDir.resolve("application.yml");

        Files.writeString(configFile, """
                server:
                  port: 8080
                  rootDir: ./www
                logging:
                  level: INFO
                """);

        AppConfig appConfig = ConfigLoader.loadOnce(configFile, new String[]{
                "--port", "80",
                "--rootDir", "./public",
                "--logLevel", "DEBUG"
        });

        assertThat(appConfig.server().port()).isEqualTo(80);
        assertThat(appConfig.server().rootDir()).isEqualTo("./public");
        assertThat(appConfig.logging().level()).isEqualTo("DEBUG");
    }

    @Test
    @DisplayName("cli arguments should override default values when config file is missing")
    void cli_overrides_defaults_when_file_is_missing(){
        Path missingFile = tempDir.resolve("missing.yml");

        AppConfig appConfig = ConfigLoader.loadOnce(missingFile, new String[]{
                "--port", "3000"
        });

        assertThat(appConfig.server().port()).isEqualTo(3000);
        assertThat(appConfig.server().rootDir()).isEqualTo("./www"); //default
        assertThat(appConfig.logging().level()).isEqualTo("INFO"); //default
    }

    @Test
    @DisplayName("a partial CLI override should only change specified values")
    void cli_overrides_only_changes_specified_values() throws IOException {
        Path configFile = tempDir.resolve("application.yml");
        Files.writeString(configFile, """
                server:
                  port: 9090
                  rootDir: ./site
                logging:
                  level: DEBUG
                """);

        AppConfig appConfig = ConfigLoader.loadOnce(configFile, new String[]{
                "--port", "3030"
        });

        assertThat(appConfig.server().port()).isEqualTo(3030); //CLI override
        assertThat(appConfig.server().rootDir()).isEqualTo("./site"); // from file
        assertThat(appConfig.logging().level()).isEqualTo("DEBUG"); // from file
    }
}
