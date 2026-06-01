package com.fitness.modules.knowledge.rerank;

import com.fitness.modules.knowledge.rerank.impl.HttpRerankerServiceImpl;
import com.fitness.modules.knowledge.rerank.model.RerankCandidate;
import com.fitness.modules.knowledge.rerank.model.RerankRequest;
import com.fitness.modules.knowledge.rerank.model.RerankResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class HttpRerankerServiceImplTest {

    @Test
    void rerankShouldPostCandidatesAndMapResponseScores() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://reranker.local/rerank"))
                .andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "query": "membership transfer",
                          "documents": [
                            {"id": "1", "text": "general fitness rule"},
                            {"id": "2", "text": "membership transfer is not allowed"}
                          ],
                          "topN": 2
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {"id": "2", "score": 0.94},
                            {"id": "1", "score": 0.12}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        HttpRerankerServiceImpl service = new HttpRerankerServiceImpl(
                builder.build(),
                "http://reranker.local/rerank");

        List<RerankResult> results = service.rerank(request(
                "membership transfer",
                candidate(1L, "general fitness rule", 0.9, 1),
                candidate(2L, "membership transfer is not allowed", 0.4, 2)));

        assertEquals(2, results.size());
        assertEquals(2L, results.get(0).getChunkId());
        assertEquals(0.94, results.get(0).getScore());
        assertEquals(1, results.get(0).getRank());
        assertEquals(1L, results.get(1).getChunkId());
        assertEquals(0.12, results.get(1).getScore());
        assertEquals(2, results.get(1).getRank());
        server.verify();
    }

    private RerankRequest request(String query, RerankCandidate... candidates) {
        RerankRequest request = new RerankRequest();
        request.setQuery(query);
        request.setCandidates(List.of(candidates));
        request.setTopN(2);
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
