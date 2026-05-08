package com.fitness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

final class EnvFilePropertyLoader {

    private EnvFilePropertyLoader() {
    }

    static void load(Path envFile) {
        if (!Files.exists(envFile)) {
            return;
        }

        try (Stream<String> lines = Files.lines(envFile, StandardCharsets.UTF_8)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .forEach(EnvFilePropertyLoader::setSystemProperty);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load .env file: " + envFile.toAbsolutePath(), e);
        }
    }

    private static void setSystemProperty(String line) {
        int separatorIndex = line.indexOf('=');
        if (separatorIndex <= 0) {
            return;
        }

        String key = line.substring(0, separatorIndex).trim();
        if (key.isEmpty()) {
            return;
        }

        String value = line.substring(separatorIndex + 1).trim();
        System.setProperty(key, value);
    }
}
