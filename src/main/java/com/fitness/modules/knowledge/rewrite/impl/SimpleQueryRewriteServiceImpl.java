package com.fitness.modules.knowledge.rewrite.impl;

import cn.hutool.core.util.StrUtil;
import com.fitness.modules.knowledge.rewrite.QueryRewriteService;
import com.fitness.modules.knowledge.rewrite.model.QueryRewriteRequest;
import com.fitness.modules.knowledge.rewrite.model.QueryRewriteResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@ConditionalOnProperty(prefix = "knowledge.rag.query-rewrite", name = "provider", havingValue = "simple")
public class SimpleQueryRewriteServiceImpl implements QueryRewriteService {

    @Override
    public QueryRewriteResult rewrite(QueryRewriteRequest request) {
        String query = request.getQuery();
        Set<String> queries = new LinkedHashSet<>();
        queries.add(query);
        queries.addAll(expand(query));

        QueryRewriteResult result = new QueryRewriteResult();
        result.setOriginalQuery(query);
        result.setQueries(limit(queries, normalizeMaxQueries(request.getMaxQueries())));
        result.setStrategy("simple");
        return result;
    }

    private List<String> expand(String query) {
        String normalizedQuery = StrUtil.nullToEmpty(query);
        List<String> expanded = new ArrayList<>();
        if (containsAny(normalizedQuery, "课程", "课", "团课", "私教")) {
            expanded.add("团课 私教 课程 预约");
            expanded.add("健身课程 类型 适合人群");
        } else if (containsAny(normalizedQuery, "会员卡", "办卡", "卡")) {
            expanded.add("会员卡 权益 规则");
            expanded.add("会员卡 转让 借用 退款 有效期");
        } else if (containsAny(normalizedQuery, "退款", "退费")) {
            expanded.add("退款 退费 会员卡 规则");
            expanded.add("办卡 退款 条件");
        } else if (containsAny(normalizedQuery, "预约", "取消", "迟到")) {
            expanded.add("课程预约 取消 迟到 规则");
            expanded.add("团课预约 满员 候补");
        }
        return expanded;
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private int normalizeMaxQueries(Integer maxQueries) {
        if (maxQueries == null || maxQueries <= 0) {
            return 3;
        }
        return Math.min(maxQueries, 5);
    }

    private List<String> limit(Set<String> queries, int maxQueries) {
        return queries.stream()
                .filter(StrUtil::isNotBlank)
                .limit(maxQueries)
                .toList();
    }
}
