package com.fitness.integration.ai.prompt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptTemplatesTest {

    private final PromptTemplates promptTemplates = new PromptTemplates();

    @Test
    void createFitnessPlanPromptShouldSeparateSystemAndUserContent() {
        AiPromptSpec prompt = promptTemplates.createFitnessPlanPrompt(Map.of(
                "height", 175,
                "weight", 70,
                "age", 28,
                "goal", "muscle gain",
                "experience", "beginner",
                "availableCourses", "[{\"id\":1,\"name\":\"course-a\"}]",
                "availableEquipment", "[{\"id\":2,\"name\":\"treadmill\"}]"
        ));

        assertFalse(prompt.system().isBlank());
        assertTrue(prompt.system().length() > 40);
        assertTrue(prompt.user().contains("175"));
        assertTrue(prompt.user().contains("weeklyPlan"));
        assertTrue(prompt.user().contains("course-a"));
        assertTrue(prompt.user().contains("treadmill"));
    }

    @Test
    void createTextPolishPromptShouldKeepOriginalTextInUser() {
        AiPromptSpec prompt = promptTemplates.createTextPolishPrompt("raw copy");

        assertFalse(prompt.system().isBlank());
        assertTrue(prompt.user().contains("raw copy"));
    }
}
