package ru.paul.wincustomf3.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

public final class ConfigManager {

    private final Path dataDirectory;
    private final Path configPath;
    private final Yaml yaml;

    public ConfigManager(final Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configPath = dataDirectory.resolve("config.yml");

        final LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        this.yaml = new Yaml(new SafeConstructor(loaderOptions));
    }

    public PluginConfig load() throws ConfigLoadException {
        try {
            Files.createDirectories(dataDirectory);
            ensureDefaultConfig();
        } catch (final IOException exception) {
            throw new ConfigLoadException("Не удалось подготовить директорию плагина.", exception);
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            final Object loadedObject = yaml.load(reader);
            if (loadedObject == null) {
                return PluginConfig.defaultConfig();
            }

            if (!(loadedObject instanceof Map<?, ?> rootMap)) {
                throw new ConfigLoadException("Корень config.yml должен быть YAML-объектом.");
            }

            return parseConfig(rootMap);
        } catch (final IOException | YAMLException exception) {
            throw new ConfigLoadException("Не удалось прочитать config.yml.", exception);
        }
    }

    private void ensureDefaultConfig() throws IOException, ConfigLoadException {
        if (Files.exists(configPath)) {
            return;
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (inputStream == null) {
                throw new ConfigLoadException("Внутренний config.yml не найден в ресурсах плагина.");
            }

            Files.copy(inputStream, configPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private PluginConfig parseConfig(final Map<?, ?> rootMap) {
        final String brand = readString(rootMap, "brand", PluginConfig.defaultBrand);

        final Map<?, ?> animationMap = readMap(rootMap, "animation");
        final boolean animationEnabled = readBoolean(animationMap, "enabled", false);
        final long intervalMillis = Math.max(1L, readLong(animationMap, "interval-ms", 500L));
        final List<String> frames = readStringList(animationMap, "frames");

        return new PluginConfig(brand, new AnimationConfig(animationEnabled, intervalMillis, frames));
    }

    private Map<?, ?> readMap(final Map<?, ?> rootMap, final String key) {
        final Object value = rootMap.get(key);
        if (value instanceof Map<?, ?> mapValue) {
            return mapValue;
        }

        return Map.of();
    }

    private String readString(final Map<?, ?> rootMap, final String key, final String defaultValue) {
        final Object value = rootMap.get(key);
        if (value == null) {
            return defaultValue;
        }

        final String stringValue = String.valueOf(value).trim();
        return stringValue.isEmpty() ? defaultValue : stringValue;
    }

    private boolean readBoolean(final Map<?, ?> rootMap, final String key, final boolean defaultValue) {
        final Object value = rootMap.get(key);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }

        return defaultValue;
    }

    private long readLong(final Map<?, ?> rootMap, final String key, final long defaultValue) {
        final Object value = rootMap.get(key);
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }

        if (value instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue.trim());
            } catch (final NumberFormatException ignored) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    private List<String> readStringList(final Map<?, ?> rootMap, final String key) {
        final Object value = rootMap.get(key);
        if (!(value instanceof Iterable<?> iterable)) {
            return List.of();
        }

        final List<String> result = new ArrayList<>();
        for (final Object entry : iterable) {
            if (entry == null) {
                continue;
            }

            final String stringValue = String.valueOf(entry).trim();
            if (!stringValue.isEmpty()) {
                result.add(stringValue);
            }
        }

        return result;
    }
}
