package com.fitness.modules.knowledge.model.vo;

import lombok.Data;

@Data
public class RAGDebugChunkVO {
    private Long chunkId;

    private Long documentId;

    private String documentTitle;

    private String content;

    private String contentPreview;

    private Double vectorSimilarity;

    private Double keywordScore;

    private Double rrfScore;

    private Double finalScore;

    private Double rerankScore;

    private Integer source;

    private Integer rank;
}
