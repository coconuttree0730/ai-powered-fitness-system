package com.fitness.modules.knowledge.rerank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.modules.knowledge.rerank.impl.HttpRerankerServiceImpl;
import com.fitness.modules.knowledge.rerank.model.RerankCandidate;
import com.fitness.modules.knowledge.rerank.model.RerankRequest;
import com.fitness.modules.knowledge.rerank.model.RerankResult;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRerankerServiceImplTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.createContext("/rerank", exchange -> {
            String response = """
                    {"results": [{"id": "2", "score": 0.94}, {"id": "1", "score": 0.12}]}
                    """;
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        });
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void rerankShouldPostCandidatesAndMapResponseScores() {
        HttpRerankerServiceImpl service = new HttpRerankerServiceImpl(
                "http://localhost:" + port + "/rerank", 3000, new ObjectMapper());

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
