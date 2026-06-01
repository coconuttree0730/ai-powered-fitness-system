package com.fitness.modules.knowledge.rerank.model;

import lombok.Data;

@Data
public class RerankResult {
    private Long chunkId;

    private Double score;

    private Integer rank;
}
