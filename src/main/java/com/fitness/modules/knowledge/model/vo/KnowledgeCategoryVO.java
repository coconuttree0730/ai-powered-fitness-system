package com.fitness.modules.knowledge.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeCategoryVO {
    private Long id;

    private String name;

    private String code;

    private String description;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
