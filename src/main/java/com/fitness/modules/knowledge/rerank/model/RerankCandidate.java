package com.fitness.modules.knowledge.rerank.model;

import lombok.Data;

@Data
public class RerankCandidate {
    private Long chunkId;

    private String content;

    private Double originalScore;

    private Integer originalRank;
}
