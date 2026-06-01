package com.fitness.modules.ai.evaluation.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class AIEvaluationResultVO {
    private Integer total;

    private Integer passed;

    private Double hitRate;

    private Long averageRetrievalTimeMs;

    private Long averageTotalTimeMs;

    private List<AIEvaluationCaseResultVO> cases;
}
