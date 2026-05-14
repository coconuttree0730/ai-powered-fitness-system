package com.fitness.integration.ai.support;

import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiSupportTest {

    @Test
    void sanitizeJsonShouldRemoveMarkdownFence() {
        AiResponseSanitizer sanitizer = new AiResponseSanitizer();
        assertEquals("{\"weeklyPlan\":[]}", sanitizer.cleanJsonResponse("```json\n{\"weeklyPlan\":[]}\n```"));
    }

    @Test
    void validatorShouldRejectMissingWeeklyPlan() {
        AiFitnessPlanValidator validator = new AiFitnessPlanValidator();
        FitnessPlanResponseDTO dto = new FitnessPlanResponseDTO();
        assertThrows(IllegalArgumentException.class, () -> validator.validateFitnessPlan(dto));
    }
}
