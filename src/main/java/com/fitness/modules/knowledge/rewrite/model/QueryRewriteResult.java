package com.fitness.modules.knowledge.rewrite.model;

import lombok.Data;

import java.util.List;

@Data
public class QueryRewriteResult {
    private String originalQuery;

    private List<String> queries;

    private String strategy;
}
