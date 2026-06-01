package com.fitness.modules.ai.evaluation.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIEvaluationRunDTO {
    private List<String> caseIds;

    private Integer topK = 5;

    private Double similarityThreshold = 0.7;
}
