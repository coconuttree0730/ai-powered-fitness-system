package com.fitness.modules.knowledge.rewrite;

import com.fitness.modules.knowledge.rewrite.model.QueryRewriteRequest;
import com.fitness.modules.knowledge.rewrite.model.QueryRewriteResult;

public interface QueryRewriteService {
    QueryRewriteResult rewrite(QueryRewriteRequest request);
}
