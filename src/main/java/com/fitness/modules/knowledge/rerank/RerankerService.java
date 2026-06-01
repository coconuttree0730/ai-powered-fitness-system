package com.fitness.modules.knowledge.rerank;

import com.fitness.modules.knowledge.rerank.model.RerankRequest;
import com.fitness.modules.knowledge.rerank.model.RerankResult;

import java.util.List;

public interface RerankerService {
    List<RerankResult> rerank(RerankRequest request);
}
