package com.fitness.modules.ai.evaluation.model;

import lombok.Data;

import java.util.List;

@Data
public class EvaluationCase {
    private String id;

    private String type;

    private String difficulty;

    private String categoryCode;

    private String question;

    private String expectedDocumentTitle;

    private List<String> expectedKeywords;

    private String notes;
}
