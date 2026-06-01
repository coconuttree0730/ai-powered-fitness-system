package com.fitness.modules.ai.evaluation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.modules.ai.evaluation.model.EvaluationCase;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationCasesResourceTest {

    @Test
    void ragAgentEvaluationCasesShouldContainAtLeastTwentyQuestions() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<EvaluationCase> cases = objectMapper.readValue(
                new ClassPathResource("evaluation/rag-agent-eval-cases.json").getInputStream(),
                new TypeReference<>() {
                });

        assertTrue(cases.size() >= 20);
    }
}
