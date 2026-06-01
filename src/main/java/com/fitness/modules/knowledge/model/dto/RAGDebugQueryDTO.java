package com.fitness.modules.knowledge.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RAGDebugQueryDTO extends RAGQueryDTO {
    private String categoryCode;
}
