package com.fitness.modules.knowledge.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RAGQueryDTO {
    @NotBlank(message = "查询内容不能为空")
    private String query;

    private Integer topK = 5;

    private Double similarityThreshold = 0.7;

    private Long categoryId;

    private boolean useKeywordSearch = true;

    private boolean useVectorSearch = true;
}
