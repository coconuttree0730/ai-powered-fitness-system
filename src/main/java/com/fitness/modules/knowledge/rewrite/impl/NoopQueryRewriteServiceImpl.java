package com.fitness.modules.knowledge.rewrite.impl;

import com.fitness.modules.knowledge.rewrite.QueryRewriteService;
import com.fitness.modules.knowledge.rewrite.model.QueryRewriteRequest;
import com.fitness.modules.knowledge.rewrite.model.QueryRewriteResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(
        prefix = "knowledge.rag.query-rewrite",
        name = "provider",
        havingValue = "noop",
        matchIfMissing = true)
public class NoopQueryRewriteServiceImpl implements QueryRewriteService {

    @Override
    public QueryRewriteResult rewrite(QueryRewriteRequest request) {
        QueryRewriteResult result = new QueryRewriteResult();
        result.setOriginalQuery(request.getQuery());
        result.setQueries(List.of(request.getQuery()));
        result.setStrategy("noop");
        return result;
    }
}
