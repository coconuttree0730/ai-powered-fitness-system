package com.fitness.modules.knowledge.model.dto;

import lombok.Data;

@Data
public class KnowledgeDocumentQueryDTO {
    private String keyword;

    private Long categoryId;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
