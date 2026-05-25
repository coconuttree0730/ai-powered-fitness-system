package com.fitness.modules.knowledge.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class RAGSearchResultVO {
    private String query;

    private List<RetrievedChunk> chunks;

    private String answer;

    private Long totalTimeMs;

    private Long retrievalTimeMs;

    private Long generationTimeMs;

    @Data
    public static class RetrievedChunk {
        private Long chunkId;

        private Long documentId;

        private String documentTitle;

        private String content;

        private Double similarity;

        private String categoryName;

        private Integer source;
    }
}
