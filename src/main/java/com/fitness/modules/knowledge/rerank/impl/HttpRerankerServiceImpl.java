package com.fitness.modules.knowledge.rerank.impl;

import cn.hutool.core.collection.CollUtil;
import com.fitness.modules.knowledge.rerank.RerankerService;
import com.fitness.modules.knowledge.rerank.model.RerankCandidate;
import com.fitness.modules.knowledge.rerank.model.RerankRequest;
import com.fitness.modules.knowledge.rerank.model.RerankResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

@Service
@ConditionalOnProperty(prefix = "knowledge.rag.reranker", name = "provider", havingValue = "http")
@Slf4j
public class HttpRerankerServiceImpl implements RerankerService {

    private final RestClient restClient;
    private final String endpoint;

    public HttpRerankerServiceImpl(
            RestClient.Builder restClientBuilder,
            @Value("${knowledge.rag.reranker.endpoint:http://localhost:8081/rerank}") String endpoint,
            @Value("${knowledge.rag.reranker.timeout-ms:3000}") int timeoutMs) {
        this(restClientBuilder
                .requestFactory(requestFactory(timeoutMs))
                .build(), endpoint);
    }

    public HttpRerankerServiceImpl(RestClient restClient, String endpoint) {
        this.endpoint = endpoint;
        this.restClient = restClient;
    }

    @Override
    public List<RerankResult> rerank(RerankRequest request) {
        if (request == null || CollUtil.isEmpty(request.getCandidates())) {
            return List.of();
        }

        HttpRerankResponse response = restClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(toHttpRequest(request))
                .retrieve()
                .body(HttpRerankResponse.class);
        if (response == null || CollUtil.isEmpty(response.results())) {
            return List.of();
        }

        return IntStream.range(0, response.results().size())
                .mapToObj(index -> toRerankResult(response.results().get(index), index + 1))
                .filter(result -> result.getChunkId() != null)
                .toList();
    }

    private static SimpleClientHttpRequestFactory requestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofMillis(Math.max(timeoutMs, 1));
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        return requestFactory;
    }

    private HttpRerankRequest toHttpRequest(RerankRequest request) {
        List<HttpRerankDocument> documents = request.getCandidates().stream()
                .map(this::toHttpDocument)
                .toList();
        return new HttpRerankRequest(request.getQuery(), documents, request.getTopN());
    }

    private HttpRerankDocument toHttpDocument(RerankCandidate candidate) {
        return new HttpRerankDocument(String.valueOf(candidate.getChunkId()), candidate.getContent());
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
