package org.example.config;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ConfigLoader {

    private static volatile AppConfig cached;

    private ConfigLoader() {}

    public static AppConfig loadOnce(Path configPath) {
        if (cached != null) return cached;

        synchronized (ConfigLoader.class) {
            if (cached == null){
                cached = load(configPath).withDefaultsApplied();
            }
            return cached;
        }
    }

    public static AppConfig get(){
        if (cached == null){
            throw new IllegalStateException("Config not loaded. call ConfigLoader.loadOnce(...) at startup.");
        }
        return cached;

    }

    public static AppConfig load(Path configPath) {
        Objects.requireNonNull(configPath, "configPath");

        if (!Files.exists(configPath)) {
            return AppConfig.defaults();
        }

        ObjectMapper objectMapper = createMapperFor(configPath);

        try (InputStream stream = Files.newInputStream(configPath)){
            AppConfig config = objectMapper.readValue(stream, AppConfig.class);
            return config == null ? AppConfig.defaults() : config;
        } catch (IOException e){
            throw new IllegalStateException("failed to read config file " + configPath.toAbsolutePath(), e);
        }
    }

    private static ObjectMapper createMapperFor(Path configPath) {
        String name = configPath.getFileName().toString().toLowerCase();

        if (name.endsWith(".yml") || name.endsWith(".yaml")) {
            return YAMLMapper.builder(new YAMLFactory()).build();

        } else if (name.endsWith(".json")) {
            return JsonMapper.builder().build();
        } else  {
            return YAMLMapper.builder(new YAMLFactory()).build();
        }
    }
}

