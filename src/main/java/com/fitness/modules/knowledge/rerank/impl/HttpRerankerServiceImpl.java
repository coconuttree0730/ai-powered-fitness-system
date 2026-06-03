package com.fitness.modules.knowledge.rerank.impl;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.modules.knowledge.rerank.RerankerService;
import com.fitness.modules.knowledge.rerank.model.RerankCandidate;
import com.fitness.modules.knowledge.rerank.model.RerankRequest;
import com.fitness.modules.knowledge.rerank.model.RerankResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

@Service
@ConditionalOnProperty(prefix = "knowledge.rag.reranker", name = "provider", havingValue = "http")
@Slf4j
public class HttpRerankerServiceImpl implements RerankerService {

    private final String endpoint;
    private final int timeoutMs;
    private final ObjectMapper objectMapper;

    @Autowired
    public HttpRerankerServiceImpl(
            ObjectMapper objectMapper,
            @Value("${knowledge.rag.reranker.endpoint:http://localhost:8081/rerank}") String endpoint,
            @Value("${knowledge.rag.reranker.timeout-ms:10000}") int timeoutMs) {
        this.endpoint = endpoint;
        this.timeoutMs = timeoutMs;
        this.objectMapper = objectMapper;
    }

    public HttpRerankerServiceImpl(String endpoint, int timeoutMs, ObjectMapper objectMapper) {
        this.endpoint = endpoint;
        this.timeoutMs = timeoutMs;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<RerankResult> rerank(RerankRequest request) {
        if (request == null || CollUtil.isEmpty(request.getCandidates())) {
            return List.of();
        }

        try {
            String requestJson = objectMapper.writeValueAsString(toHttpRequest(request));
            log.info("Reranker request: {} bytes, {} candidates", requestJson.length(), request.getCandidates().size());

            HttpURLConnection conn = (HttpURLConnection) URI.create(endpoint).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestJson.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                log.warn("Reranker returned HTTP {}", status);
                return List.of();
            }

            String responseJson = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            HttpRerankResponse response = objectMapper.readValue(responseJson, HttpRerankResponse.class);

            if (response == null || CollUtil.isEmpty(response.results())) {
                return List.of();
            }

            return IntStream.range(0, response.results().size())
                    .mapToObj(index -> toRerankResult(response.results().get(index), index + 1))
                    .filter(result -> result.getChunkId() != null)
                    .toList();
        } catch (Exception ex) {
            log.warn("Reranker HTTP call failed: {}", ex.getMessage());
            return List.of();
        }
    }

    private HttpRerankRequest toHttpRequest(RerankRequest request) {
        List<HttpRerankDocument> documents = request.getCandidates().stream()
                .map(this::toHttpDocument)
                .toList();
        return new HttpRerankRequest(request.getQuery(), documents, request.getTopN());
    }

    private HttpRerankDocument toHttpDocument(RerankCandidate candidate) {
        // 截断内容以避免请求体过大导致超时
        String content = candidate.getContent();
        if (content != null && content.length() > 500) {
            content = content.substring(0, 500);
        }
        return new HttpRerankDocument(String.valueOf(candidate.getChunkId()), content);
    }

    private RerankResult toRerankResult(HttpRerankItem item, int rank) {
        RerankResult result = new RerankResult();
        result.setChunkId(parseChunkId(item.id()));
        result.setScore(item.score());
        result.setRank(rank);
        return result;
    }

    private Long parseChunkId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            log.warn("Reranker returned invalid chunk id: {}", id);
            return null;
        }
    }

    private record HttpRerankRequest(String query, List<HttpRerankDocument> documents, Integer topN) {
    }

    private record HttpRerankDocument(String id, String text) {
    }

    private record HttpRerankResponse(List<HttpRerankItem> results) {
    }

    private record HttpRerankItem(String id, Double score) {
    }
}
