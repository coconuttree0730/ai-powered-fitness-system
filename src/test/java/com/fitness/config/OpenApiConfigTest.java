package com.fitness.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    @Test
    void shouldExposeBearerSecuritySchemeAndMetadata() {
        OpenApiConfig config = new OpenApiConfig();

        OpenAPI openAPI = config.fitnessOpenAPI();

        assertNotNull(openAPI.getInfo());
        assertEquals("AI 健身房管理系统 API", openAPI.getInfo().getTitle());
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("bearerAuth"));
    }
}
