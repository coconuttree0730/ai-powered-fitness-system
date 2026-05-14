package com.fitness.integration.ai.support;

import org.springframework.stereotype.Component;

@Component
public class AiResponseSanitizer {

    public String cleanJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return response;
        }

        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    public String cleanPolishedResponse(String response) {
        if (response == null || response.isBlank()) {
            return response;
        }

        String result = response.trim();
        result = result.replaceAll("[（(]\\s*(?:约\\s*)?\\d+\\s*(?:字|词|words?)\\s*[）)]", "");
        result = result.replaceAll("[【\\[]\\s*\\d+\\s*(?:字|词|words?)\\s*[】\\]]", "");
        result = result.replaceAll("\\n{3,}", "\\n\\n");
        return result.trim();
    }
}
