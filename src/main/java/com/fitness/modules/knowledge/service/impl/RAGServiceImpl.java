package com.fitness.modules.knowledge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.integration.ai.prompt.AiPromptSpec;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.chat.prompt.ChatPromptTemplates;
import com.fitness.modules.knowledge.mapper.KnowledgeCategoryMapper;
import com.fitness.modules.knowledge.model.dto.RAGDebugQueryDTO;
import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.entity.KnowledgeCategory;
import com.fitness.modules.knowledge.model.vo.KnowledgeChunkVO;
import com.fitness.modules.knowledge.model.vo.RAGDebugChunkVO;
import com.fitness.modules.knowledge.model.vo.RAGDebugResultVO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.EmbeddingService;
import com.fitness.modules.knowledge.service.KnowledgeChunkService;
import com.fitness.modules.knowledge.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAGServiceImpl implements RAGService {

    private static final int RRF_K = 60;
    private static final String RAG_FALLBACK_ANSWER =
            "\u62b1\u6b49\uff0c\u6211\u6682\u65f6\u65e0\u6cd5\u627e\u5230\u76f8\u5173\u4fe1\u606f\u6765\u56de\u7b54\u60a8\u7684\u95ee\u9898\u3002";

    private final EmbeddingService embeddingService;
    private final KnowledgeChunkService chunkService;
    private final KnowledgeCategoryMapper categoryMapper;
    private final AIService aiService;
    private final ChatPromptTemplates chatPromptTemplates;
    @Qualifier("ioTaskExecutor")
    private final Executor ioTaskExecutor;

    @Override
    public RAGSearchResultVO search(RAGQueryDTO queryDTO) {
        long startTime = System.currentTimeMillis();

        log.info("RAG search started, query='{}'", queryDTO.getQuery());
        log.info("RAG search params: topK={}, similarityThreshold={}, categoryId={}",
                queryDTO.getTopK(), queryDTO.getSimilarityThreshold(), queryDTO.getCategoryId());

        RAGSearchResultVO result = new RAGSearchResultVO();
        result.setQuery(queryDTO.getQuery());

        List<RAGSearchResultVO.RetrievedChunk> chunks = hybridSearch(
                queryDTO.getQuery(),
                queryDTO.getTopK(),
                queryDTO.getCategoryId(),
                queryDTO.getSimilarityThreshold()
        );

        result.setChunks(chunks);

        long retrievalTime = System.currentTimeMillis() - startTime;
        result.setRetrievalTimeMs(retrievalTime);
        log.info("RAG retrieval completed in {} ms, chunks={}", retrievalTime, chunks.size());

        if (CollUtil.isNotEmpty(chunks)) {
            String context = buildContext(chunks);
            log.info("RAG context built, length={}", context.length());

            String answer = generateAnswer(queryDTO.getQuery(), context);
            result.setAnswer(answer);
            log.info("RAG answer generated, length={}", answer != null ? answer.length() : 0);
        } else {
            log.warn("RAG retrieval returned no relevant chunks");
        }

        long totalTime = System.currentTimeMillis() - startTime;
        result.setTotalTimeMs(totalTime);
        result.setGenerationTimeMs(totalTime - retrievalTime);

        log.info("RAG search finished, total={} ms, retrieval={} ms, generation={} ms",
                totalTime, retrievalTime, totalTime - retrievalTime);

        return result;
    }

    @Override
    public String chatWithRAG(String query, Long categoryId) {
        log.info("RAG chat request, query='{}'", query);

        RAGQueryDTO queryDTO = new RAGQueryDTO();
        queryDTO.setQuery(query);
        queryDTO.setTopK(5);
        queryDTO.setSimilarityThreshold(0.6);
        queryDTO.setCategoryId(categoryId);

        RAGSearchResultVO result = search(queryDTO);
        return StrUtil.isNotBlank(result.getAnswer()) ? result.getAnswer() : RAG_FALLBACK_ANSWER;
    }

    @Override
    public List<RAGSearchResultVO.RetrievedChunk> hybridSearch(
            String query, Integer topK, Long categoryId, Double similarityThreshold) {
        log.info("Hybrid retrieval started, query='{}', topK={}", query, topK);
        int effectiveTopK = normalizeTopK(topK);

        // 关键词搜索与embedding+向量搜索并行
        CompletableFuture<List<KnowledgeChunkVO>> keywordFuture = CompletableFuture.supplyAsync(
            () -> chunkService.keywordSearch(query, effectiveTopK * 2, categoryId), ioTaskExecutor);

        List<KnowledgeChunkVO> vectorResults = Collections.emptyList();

        long vectorStart = System.currentTimeMillis();
        float[] queryEmbedding = embeddingService.embed(query);
        if (queryEmbedding != null && queryEmbedding.length > 0) {
            log.info("Query embedding completed, dimensions={}, elapsed={} ms",
                    queryEmbedding.length, System.currentTimeMillis() - vectorStart);
            vectorResults = chunkService.vectorSearch(queryEmbedding, effectiveTopK * 2, categoryId, similarityThreshold);
            log.info("Vector retrieval completed, results={}", vectorResults.size());

            for (int i = 0; i < Math.min(vectorResults.size(), effectiveTopK); i++) {
                KnowledgeChunkVO chunk = vectorResults.get(i);
                log.debug("Vector result #{} title='{}' similarity={} preview={}",
                        i + 1,
                        chunk.getDocumentTitle(),
                        chunk.getSimilarity(),
                        preview(chunk.getContent()));
            }
        } else {
            log.warn("Query embedding failed");
        }

        // 等待关键词搜索完成
        List<KnowledgeChunkVO> keywordResults = keywordFuture.join();
        log.info("Keyword retrieval completed, results={}", keywordResults.size());

        for (int i = 0; i < Math.min(keywordResults.size(), effectiveTopK); i++) {
            KnowledgeChunkVO chunk = keywordResults.get(i);
            log.debug("Keyword result #{} title='{}' preview={}",
                    i + 1,
                    chunk.getDocumentTitle(),
                    preview(chunk.getContent()));
        }

        List<RAGSearchResultVO.RetrievedChunk> finalResults =
                mergeResults(vectorResults, keywordResults, effectiveTopK);

        log.info("Hybrid retrieval merged, finalResults={}", finalResults.size());
        for (int i = 0; i < finalResults.size(); i++) {
            RAGSearchResultVO.RetrievedChunk chunk = finalResults.get(i);
            String sourceType = chunk.getSource() == 1 ? "vector" : (chunk.getSource() == 2 ? "keyword" : "hybrid");
            log.debug("Top-{} source={} title='{}' score={} content={}",
                    i + 1, sourceType, chunk.getDocumentTitle(), chunk.getSimilarity(), chunk.getContent());
        }

        return finalResults;
    }

    private List<RAGSearchResultVO.RetrievedChunk> mergeResults(
            List<KnowledgeChunkVO> vectorResults,
            List<KnowledgeChunkVO> keywordResults,
            int topK) {
        Map<Long, RAGSearchResultVO.RetrievedChunk> mergedResults = new LinkedHashMap<>();

        for (int i = 0; i < vectorResults.size(); i++) {
            KnowledgeChunkVO chunk = vectorResults.get(i);
            RAGSearchResultVO.RetrievedChunk retrievedChunk = toRetrievedChunk(chunk, 1);
            double rrfScore = 1.0 / (RRF_K + i + 1);
            retrievedChunk.setVectorSimilarity(chunk.getSimilarity());
            retrievedChunk.setRrfScore(rrfScore);
            retrievedChunk.setFinalScore(rrfScore);
            retrievedChunk.setSimilarity(rrfScore);
            mergedResults.put(chunk.getId(), retrievedChunk);
            log.debug("Vector result RRF: chunkId={}, rank={}, rrfScore={}",
                    chunk.getId(), i + 1, rrfScore);
        }

        for (int i = 0; i < keywordResults.size(); i++) {
            KnowledgeChunkVO chunk = keywordResults.get(i);
            double rrfScore = 1.0 / (RRF_K + i + 1);

            if (mergedResults.containsKey(chunk.getId())) {
                RAGSearchResultVO.RetrievedChunk existing = mergedResults.get(chunk.getId());
                double existingScore = existing.getRrfScore() != null ? existing.getRrfScore() : 0;
                double finalScore = existingScore + rrfScore;
                existing.setKeywordScore(chunk.getSimilarity());
                existing.setRrfScore(finalScore);
                existing.setFinalScore(finalScore);
                existing.setSimilarity(finalScore);
                existing.setSource(3);
                log.debug("Merged keyword result: chunkId={}, previousScore={}, addedRrf={}, finalScore={}",
                        chunk.getId(), existingScore, rrfScore, finalScore);
            } else {
                RAGSearchResultVO.RetrievedChunk retrievedChunk = toRetrievedChunk(chunk, 2);
                retrievedChunk.setKeywordScore(chunk.getSimilarity());
                retrievedChunk.setRrfScore(rrfScore);
                retrievedChunk.setFinalScore(rrfScore);
                retrievedChunk.setSimilarity(rrfScore);
                mergedResults.put(chunk.getId(), retrievedChunk);
                log.debug("Added keyword result: chunkId={}, rrfScore={}", chunk.getId(), rrfScore);
            }
        }

        return mergedResults.values().stream()
                .sorted(Comparator.comparingDouble(RAGSearchResultVO.RetrievedChunk::getFinalScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    public RAGDebugResultVO debugSearch(RAGDebugQueryDTO queryDTO) {
        long startTime = System.currentTimeMillis();
        int effectiveTopK = normalizeTopK(queryDTO.getTopK());
        Long categoryId = resolveCategoryId(queryDTO.getCategoryId(), queryDTO.getCategoryCode());

        RAGDebugResultVO result = new RAGDebugResultVO();
        result.setQuery(queryDTO.getQuery());
        result.setCategoryId(categoryId);
        result.setCategoryCode(queryDTO.getCategoryCode());
        result.setTopK(effectiveTopK);
        result.setSimilarityThreshold(queryDTO.getSimilarityThreshold());
        log.info("RAG debug started, query='{}', categoryCode={}, categoryId={}, topK={}, threshold={}",
                queryDTO.getQuery(), queryDTO.getCategoryCode(), categoryId, effectiveTopK,
                queryDTO.getSimilarityThreshold());

        CompletableFuture<List<KnowledgeChunkVO>> keywordFuture = CompletableFuture.supplyAsync(
                () -> queryDTO.isUseKeywordSearch()
                        ? chunkService.keywordSearch(queryDTO.getQuery(), effectiveTopK * 2, categoryId)
                        : Collections.emptyList(),
                ioTaskExecutor);

        long embeddingStart = System.currentTimeMillis();
        float[] queryEmbedding = queryDTO.isUseVectorSearch() ? embeddingService.embed(queryDTO.getQuery()) : null;
        result.setEmbeddingTimeMs(System.currentTimeMillis() - embeddingStart);

        List<KnowledgeChunkVO> vectorResults = Collections.emptyList();
        long vectorStart = System.currentTimeMillis();
        if (queryEmbedding != null && queryEmbedding.length > 0) {
            vectorResults = chunkService.vectorSearch(
                    queryEmbedding,
                    effectiveTopK * 2,
                    categoryId,
                    queryDTO.getSimilarityThreshold());
        }
        result.setVectorSearchTimeMs(System.currentTimeMillis() - vectorStart);

        long keywordStart = System.currentTimeMillis();
        List<KnowledgeChunkVO> keywordResults = keywordFuture.join();
        result.setKeywordSearchTimeMs(System.currentTimeMillis() - keywordStart);

        List<RAGSearchResultVO.RetrievedChunk> mergedResults =
                mergeResults(vectorResults, keywordResults, effectiveTopK);

        result.setVectorChunks(toDebugChunks(vectorResults, 1));
        result.setKeywordChunks(toDebugChunks(keywordResults, 2));
        result.setMergedChunks(toDebugChunksFromRetrieved(mergedResults));
        result.setRetrievalTimeMs(System.currentTimeMillis() - startTime);
        log.info("RAG debug finished, vector={}, keyword={}, merged={}, retrieval={} ms",
                vectorResults.size(), keywordResults.size(), mergedResults.size(), result.getRetrievalTimeMs());
        return result;
    }

    private Long resolveCategoryId(Long categoryId, String categoryCode) {
        if (categoryId != null || StrUtil.isBlank(categoryCode)) {
            return categoryId;
        }
        List<KnowledgeCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeCategory>()
                        .eq(KnowledgeCategory::getDeleted, false));
        return categories.stream()
                .filter(category -> categoryCode.equalsIgnoreCase(category.getCode()))
                .map(KnowledgeCategory::getId)
                .findFirst()
                .orElse(null);
    }

    private RAGSearchResultVO.RetrievedChunk toRetrievedChunk(KnowledgeChunkVO chunk, int source) {
        RAGSearchResultVO.RetrievedChunk retrievedChunk = new RAGSearchResultVO.RetrievedChunk();
        retrievedChunk.setChunkId(chunk.getId());
        retrievedChunk.setDocumentId(chunk.getDocumentId());
        retrievedChunk.setDocumentTitle(chunk.getDocumentTitle());
        retrievedChunk.setContent(chunk.getContent());
        retrievedChunk.setSimilarity(chunk.getSimilarity());
        retrievedChunk.setSource(source);

        if (chunk.getMetadata() != null) {
            Object categoryIdObj = chunk.getMetadata().get("category_id");
            if (categoryIdObj != null) {
                try {
                    Long catId = Long.valueOf(categoryIdObj.toString());
                    KnowledgeCategory category = categoryMapper.selectById(catId);
                    if (category != null) {
                        retrievedChunk.setCategoryName(category.getName());
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return retrievedChunk;
    }

    private List<RAGDebugChunkVO> toDebugChunks(List<KnowledgeChunkVO> chunks, int source) {
        return IntStream.range(0, chunks.size())
                .mapToObj(index -> toDebugChunk(chunks.get(index), source, index + 1))
                .collect(Collectors.toList());
    }

    private RAGDebugChunkVO toDebugChunk(KnowledgeChunkVO chunk, int source, int rank) {
        RAGDebugChunkVO debugChunk = new RAGDebugChunkVO();
        debugChunk.setChunkId(chunk.getId());
        debugChunk.setDocumentId(chunk.getDocumentId());
        debugChunk.setDocumentTitle(chunk.getDocumentTitle());
        debugChunk.setContent(chunk.getContent());
        debugChunk.setContentPreview(preview(chunk.getContent()));
        debugChunk.setSource(source);
        debugChunk.setRank(rank);
        if (source == 1) {
            debugChunk.setVectorSimilarity(chunk.getSimilarity());
        } else if (source == 2) {
            debugChunk.setKeywordScore(chunk.getSimilarity());
        }
        return debugChunk;
    }

    private List<RAGDebugChunkVO> toDebugChunksFromRetrieved(List<RAGSearchResultVO.RetrievedChunk> chunks) {
        return IntStream.range(0, chunks.size())
                .mapToObj(index -> toDebugChunk(chunks.get(index), index + 1))
                .collect(Collectors.toList());
    }

    private RAGDebugChunkVO toDebugChunk(RAGSearchResultVO.RetrievedChunk chunk, int rank) {
        RAGDebugChunkVO debugChunk = new RAGDebugChunkVO();
        debugChunk.setChunkId(chunk.getChunkId());
        debugChunk.setDocumentId(chunk.getDocumentId());
        debugChunk.setDocumentTitle(chunk.getDocumentTitle());
        debugChunk.setContent(chunk.getContent());
        debugChunk.setContentPreview(preview(chunk.getContent()));
        debugChunk.setVectorSimilarity(chunk.getVectorSimilarity());
        debugChunk.setKeywordScore(chunk.getKeywordScore());
        debugChunk.setRrfScore(chunk.getRrfScore());
        debugChunk.setFinalScore(chunk.getFinalScore());
        debugChunk.setSource(chunk.getSource());
        debugChunk.setRank(rank);
        return debugChunk;
    }

    private int normalizeTopK(Integer topK) {
        if (topK == null || topK <= 0) {
            return 5;
        }
        return Math.min(topK, 20);
    }

    private String buildContext(List<RAGSearchResultVO.RetrievedChunk> chunks) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RAGSearchResultVO.RetrievedChunk chunk = chunks.get(i);
            context.append("[Document ").append(i + 1).append("]");
            if (StrUtil.isNotBlank(chunk.getDocumentTitle())) {
                context.append(" Source: ").append(chunk.getDocumentTitle());
            }
            context.append('\n');
            context.append(chunk.getContent()).append("\n\n");
        }
        return context.toString();
    }

    private String generateAnswer(String query, String context) {
        AiPromptSpec prompt = chatPromptTemplates.createRagPrompt(query, context);
        return aiService.chat(prompt.system(), prompt.user());
    }

    private String preview(String content) {
        if (content == null) {
            return "";
        }
        return content.substring(0, Math.min(80, content.length())) + "...";
    }
}
