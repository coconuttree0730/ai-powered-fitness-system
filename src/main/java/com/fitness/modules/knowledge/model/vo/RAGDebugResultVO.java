package com.fitness.modules.knowledge.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class RAGDebugResultVO {
    private String query;

    private Long categoryId;

    private String categoryCode;

    private Integer topK;

    private Double similarityThreshold;

    private Long embeddingTimeMs;

    private Long vectorSearchTimeMs;

    private Long keywordSearchTimeMs;

    private Long retrievalTimeMs;

    private List<RAGDebugChunkVO> vectorChunks;

    private List<RAGDebugChunkVO> keywordChunks;

    private List<RAGDebugChunkVO> mergedChunks;
}
