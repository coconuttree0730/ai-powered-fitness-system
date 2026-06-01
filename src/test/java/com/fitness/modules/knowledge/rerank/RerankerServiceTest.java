package com.fitness.modules.knowledge.rerank;

import com.fitness.modules.knowledge.rerank.impl.MockRerankerServiceImpl;
import com.fitness.modules.knowledge.rerank.impl.NoopRerankerServiceImpl;
import com.fitness.modules.knowledge.rerank.model.RerankCandidate;
import com.fitness.modules.knowledge.rerank.model.RerankRequest;
import com.fitness.modules.knowledge.rerank.model.RerankResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RerankerServiceTest {

    @Test
    void noopRerankerShouldReturnEmptyResultsForFallback() {
        RerankerService rerankerService = new NoopRerankerServiceImpl();

        List<RerankResult> results = rerankerService.rerank(request(
                "membership transfer",
                candidate(1L, "general fitness rule", 0.5, 1),
                candidate(2L, "membership transfer is not allowed", 0.4, 2)));

        assertTrue(results.isEmpty());
    }

    @Test
    void mockRerankerShouldPreferCandidateContainingQueryTerms() {
        RerankerService rerankerService = new MockRerankerServiceImpl();

        List<RerankResult> results = rerankerService.rerank(request(
                "membership transfer",
                candidate(1L, "general fitness rule", 0.9, 1),
                candidate(2L, "membership transfer is not allowed", 0.4, 2)));

        assertEquals(2, results.size());
        assertEquals(2L, results.get(0).getChunkId());
        assertEquals(1, results.get(0).getRank());
        assertTrue(results.get(0).getScore() > results.get(1).getScore());
    }

    @Test
    void mockRerankerShouldRespectTopN() {
        RerankerService rerankerService = new MockRerankerServiceImpl();
        RerankRequest request = request(
                "membership",
                candidate(1L, "membership rule one", 0.3, 1),
                candidate(2L, "membership rule two", 0.2, 2),
                candidate(3L, "membership rule three", 0.1, 3));
        request.setTopN(2);

        List<RerankResult> results = rerankerService.rerank(request);

        assertEquals(2, results.size());
    }

    private RerankRequest request(String query, RerankCandidate... candidates) {
        RerankRequest request = new RerankRequest();
        request.setQuery(query);
        request.setCandidates(List.of(candidates));
        request.setTopN(10);
        return request;
    }

    private RerankCandidate candidate(Long chunkId, String content, Double originalScore, Integer originalRank) {
        RerankCandidate candidate = new RerankCandidate();
        candidate.setChunkId(chunkId);
        candidate.setContent(content);
        candidate.setOriginalScore(originalScore);
        candidate.setOriginalRank(originalRank);
        return candidate;
    }
}
