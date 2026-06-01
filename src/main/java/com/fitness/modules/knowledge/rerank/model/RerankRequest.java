package com.fitness.modules.knowledge.rerank.model;

import lombok.Data;

import java.util.List;

@Data
public class RerankRequest {
    private String query;

    private List<RerankCandidate> candidates;

    private Integer topN;
}
