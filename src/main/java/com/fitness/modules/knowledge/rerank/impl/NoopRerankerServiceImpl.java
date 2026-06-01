package com.fitness.modules.knowledge.rerank.impl;

import com.fitness.modules.knowledge.rerank.RerankerService;
import com.fitness.modules.knowledge.rerank.model.RerankRequest;
import com.fitness.modules.knowledge.rerank.model.RerankResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@ConditionalOnProperty(
        prefix = "knowledge.rag.reranker",
        name = "provider",
        havingValue = "noop",
        matchIfMissing = true)
public class NoopRerankerServiceImpl implements RerankerService {

    @Override
    public List<RerankResult> rerank(RerankRequest request) {
        return Collections.emptyList();
    }
}
