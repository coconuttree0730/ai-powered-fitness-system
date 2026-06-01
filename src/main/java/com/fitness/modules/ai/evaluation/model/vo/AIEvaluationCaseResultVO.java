package com.fitness.modules.ai.evaluation.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class AIEvaluationCaseResultVO {
    private String id;

    private String question;

    private Boolean passed;

    private Boolean matchedDocument;

    private Boolean matchedKeyword;

    private String topDocumentTitle;

    private Boolean rerankEnabled;

    private Double topRerankScore;

    private List<String> matchedKeywords;

    private Long retrievalTimeMs;

    private Long totalTimeMs;
}
