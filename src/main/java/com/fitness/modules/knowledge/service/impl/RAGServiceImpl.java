package com.fitness.modules.knowledge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fitness.integration.ai.prompt.AiPromptSpec;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.chat.prompt.ChatPromptTemplates;
import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.vo.KnowledgeChunkVO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.EmbeddingService;
import com.fitness.modules.knowledge.service.KnowledgeCategoryService;
import com.fitness.modules.knowledge.service.KnowledgeChunkService;
import com.fitness.modules.knowledge.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAGServiceImpl implements RAGService {

    private static final int RRF_K = 60;
    private static final String RAG_FALLBACK_ANSWER =
            "\u62b1\u6b49\uff0c\u6211\u6682\u65f6\u65e0\u6cd5\u627e\u5230\u76f8\u5173\u4fe1\u606f\u6765\u56de\u7b54\u60a8\u7684\u95ee\u9898\u3002";

    private final EmbeddingService embeddingService;
    private final KnowledgeChunkService chunkService;
    private final KnowledgeCategoryService categoryService;
    private final AIService aiService;
    private final ChatPromptTemplates chatPromptTemplates;

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

        List<KnowledgeChunkVO> vectorResults = Collections.emptyList();
        List<KnowledgeChunkVO> keywordResults = Collections.emptyList();

        long vectorStart = System.currentTimeMillis();
        float[] queryEmbedding = embeddingService.embed(query);
        if (queryEmbedding != null && queryEmbedding.length > 0) {
            log.info("Query embedding completed, dimensions={}, elapsed={} ms",
                    queryEmbedding.length, System.currentTimeMillis() - vectorStart);
            vectorResults = chunkService.vectorSearch(queryEmbedding, topK * 2, categoryId, similarityThreshold);
            log.info("Vector retrieval completed, results={}", vectorResults.size());

            for (int i = 0; i < Math.min(vectorResults.size(), topK); i++) {
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

        long keywordStart = System.currentTimeMillis();
        keywordResults = chunkService.keywordSearch(query, topK * 2, categoryId);
        log.info("Keyword retrieval completed, results={}, elapsed={} ms",
                keywordResults.size(), System.currentTimeMillis() - keywordStart);

        for (int i = 0; i < Math.min(keywordResults.size(), topK); i++) {
            KnowledgeChunkVO chunk = keywordResults.get(i);
            log.debug("Keyword result #{} title='{}' preview={}",
                    i + 1,
                    chunk.getDocumentTitle(),
                    preview(chunk.getContent()));
        }

        Map<Long, RAGSearchResultVO.RetrievedChunk> mergedResults = new LinkedHashMap<>();

        for (int i = 0; i < vectorResults.size(); i++) {
            KnowledgeChunkVO chunk = vectorResults.get(i);
            RAGSearchResultVO.RetrievedChunk retrievedChunk = toRetrievedChunk(chunk, 1);
            double rrfScore = 1.0 / (RRF_K + i + 1);
            retrievedChunk.setSimilarity(chunk.getSimilarity());
            mergedResults.put(chunk.getId(), retrievedChunk);
            log.debug("Vector result RRF: chunkId={}, rank={}, rrfScore={}",
                    chunk.getId(), i + 1, rrfScore);
        }

        for (int i = 0; i < keywordResults.size(); i++) {
            KnowledgeChunkVO chunk = keywordResults.get(i);
            double rrfScore = 1.0 / (RRF_K + i + 1);

            if (mergedResults.containsKey(chunk.getId())) {
                RAGSearchResultVO.RetrievedChunk existing = mergedResults.get(chunk.getId());
                double existingScore = existing.getSimilarity() != null ? existing.getSimilarity() : 0;
                existing.setSimilarity(existingScore + rrfScore);
                existing.setSource(3);
                log.debug("Merged keyword result: chunkId={}, previousScore={}, addedRrf={}, finalScore={}",
                        chunk.getId(), existingScore, rrfScore, existingScore + rrfScore);
            } else {
                RAGSearchResultVO.RetrievedChunk retrievedChunk = toRetrievedChunk(chunk, 2);
                retrievedChunk.setSimilarity(rrfScore);
                mergedResults.put(chunk.getId(), retrievedChunk);
                log.debug("Added keyword result: chunkId={}, rrfScore={}", chunk.getId(), rrfScore);
            }
        }

        List<RAGSearchResultVO.RetrievedChunk> finalResults = mergedResults.values().stream()
                .sorted(Comparator.comparingDouble(RAGSearchResultVO.RetrievedChunk::getSimilarity).reversed())
                .limit(topK)
                .collect(Collectors.toList());

        log.info("Hybrid retrieval merged, finalResults={}", finalResults.size());
        for (int i = 0; i < finalResults.size(); i++) {
            RAGSearchResultVO.RetrievedChunk chunk = finalResults.get(i);
            String sourceType = chunk.getSource() == 1 ? "vector" : (chunk.getSource() == 2 ? "keyword" : "hybrid");
            log.debug("Top-{} source={} title='{}' score={} content={}",
                    i + 1, sourceType, chunk.getDocumentTitle(), chunk.getSimilarity(), chunk.getContent());
        }

        return finalResults;
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
                    var category = categoryService.getById(catId);
                    if (category != null) {
                        retrievedChunk.setCategoryName(category.getName());
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return retrievedChunk;
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
