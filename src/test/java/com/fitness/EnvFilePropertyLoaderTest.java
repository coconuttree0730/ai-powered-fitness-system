package com.fitness;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvFilePropertyLoaderTest {

    @Test
    void loadOverridesExistingSystemPropertyWithEnvFileValue() throws IOException {
        Path envFile = Files.createTempFile("fitness-env", ".env");
        Files.writeString(envFile, """
                # Redis
                REDIS_PASSWORD=myRedisPass123
                DB_HOST=localhost
                """);

        String originalRedisPassword = System.getProperty("REDIS_PASSWORD");
        String originalDbHost = System.getProperty("DB_HOST");

        try {
            System.setProperty("REDIS_PASSWORD", "123456");
            System.clearProperty("DB_HOST");

            EnvFilePropertyLoader.load(envFile);

            assertEquals("myRedisPass123", System.getProperty("REDIS_PASSWORD"));
            assertEquals("localhost", System.getProperty("DB_HOST"));
        } finally {
            restoreProperty("REDIS_PASSWORD", originalRedisPassword);
            restoreProperty("DB_HOST", originalDbHost);
            Files.deleteIfExists(envFile);
        }
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
            return;
        }
        System.setProperty(key, value);
    }
}
