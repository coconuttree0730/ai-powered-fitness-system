package com.fitness.modules.knowledge.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class KnowledgeChunkVO {
    private Long id;

    private Long documentId;

    private String documentTitle;

    private Integer chunkIndex;

    private String content;

    private Integer charCount;

    private Map<String, Object> metadata;

    private Double similarity;

    private LocalDateTime createTime;
}
