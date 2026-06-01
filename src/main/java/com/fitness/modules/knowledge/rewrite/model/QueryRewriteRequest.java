package com.fitness.modules.knowledge.rewrite.model;

import lombok.Data;

@Data
public class QueryRewriteRequest {
    private String query;

    private Integer maxQueries;
}
