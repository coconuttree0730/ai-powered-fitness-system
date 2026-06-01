package com.fitness.modules.knowledge.rerank.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fitness.modules.knowledge.rerank.RerankerService;
import com.fitness.modules.knowledge.rerank.model.RerankCandidate;
import com.fitness.modules.knowledge.rerank.model.RerankRequest;
import com.fitness.modules.knowledge.rerank.model.RerankResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@ConditionalOnProperty(prefix = "knowledge.rag.reranker", name = "provider", havingValue = "mock")
public class MockRerankerServiceImpl implements RerankerService {

    @Override
    public List<RerankResult> rerank(RerankRequest request) {
        if (request == null || CollUtil.isEmpty(request.getCandidates())) {
            return List.of();
        }

        int topN = normalizeTopN(request.getTopN(), request.getCandidates().size());
        List<ScoredCandidate> scoredCandidates = request.getCandidates().stream()
                .map(candidate -> new ScoredCandidate(candidate, score(request.getQuery(), candidate)))
                .sorted(Comparator.comparingDouble(ScoredCandidate::score).reversed()
                        .thenComparing(candidate -> normalizeRank(candidate.candidate().getOriginalRank())))
                .limit(topN)
                .collect(Collectors.toList());

        return IntStream.range(0, scoredCandidates.size())
                .mapToObj(index -> toResult(scoredCandidates.get(index), index + 1))
                .collect(Collectors.toList());
    }

    private double score(String query, RerankCandidate candidate) {
        Set<String> terms = terms(query);
        String content = StrUtil.nullToEmpty(candidate.getContent()).toLowerCase();
        long matchedTerms = terms.stream()
                .filter(content::contains)
                .count();
        double originalScore = candidate.getOriginalScore() == null ? 0 : candidate.getOriginalScore();
        return matchedTerms + originalScore * 0.001;
    }

    private Set<String> terms(String query) {
        return StrUtil.split(StrUtil.nullToEmpty(query).toLowerCase(), ' ').stream()
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
    }

    private int normalizeTopN(Integer topN, int candidateCount) {
        if (topN == null || topN <= 0) {
            return candidateCount;
        }
        return Math.min(topN, candidateCount);
    }

    private int normalizeRank(Integer rank) {
        return rank == null ? Integer.MAX_VALUE : rank;
    }

    private RerankResult toResult(ScoredCandidate scoredCandidate, int rank) {
        RerankResult result = new RerankResult();
        result.setChunkId(scoredCandidate.candidate().getChunkId());
        result.setScore(scoredCandidate.score());
        result.setRank(rank);
        return result;
    }

    private record ScoredCandidate(RerankCandidate candidate, double score) {
    }
}
