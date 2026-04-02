package com.fitness.modules.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.vo.KnowledgeChunkVO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAGServiceImpl implements RAGService {

    private final EmbeddingService embeddingService;
    private final KnowledgeChunkService chunkService;
    private final KnowledgeCategoryService categoryService;
    private final AIService aiService;

    private static final int RRF_K = 60;

    @Override
    public RAGSearchResultVO search(RAGQueryDTO queryDTO) {
        long startTime = System.currentTimeMillis();

        log.info("【RAG搜索】开始处理查询，查询内容: '{}'", queryDTO.getQuery());
        log.info("【RAG搜索】参数: topK={}, similarityThreshold={}, categoryId={}",
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

        log.info("【RAG搜索】检索阶段完成，耗时: {}ms，获取到 {} 个相关切片", retrievalTime, chunks.size());

        if (CollUtil.isNotEmpty(chunks)) {
            String context = buildContext(chunks);
            log.info("【RAG搜索】构建的上下文长度: {} 字符", context.length());

            String answer = generateAnswer(queryDTO.getQuery(), context);
            result.setAnswer(answer);
            log.info("【RAG搜索】AI回答生成完成，回答长度: {} 字符", answer != null ? answer.length() : 0);
        } else {
            log.warn("【RAG搜索】未找到相关切片，无法生成回答");
        }

        long totalTime = System.currentTimeMillis() - startTime;
        result.setTotalTimeMs(totalTime);
        result.setGenerationTimeMs(totalTime - retrievalTime);

        log.info("【RAG搜索】搜索完成，总耗时: {}ms (检索: {}ms, 生成: {}ms)",
                totalTime, retrievalTime, totalTime - retrievalTime);

        return result;
    }

    @Override
    public String chatWithRAG(String query, Long categoryId) {
        log.info("【RAG对话】用户查询: '{}'", query);

        RAGQueryDTO queryDTO = new RAGQueryDTO();
        queryDTO.setQuery(query);
        queryDTO.setTopK(5);
        queryDTO.setSimilarityThreshold(0.6);
        queryDTO.setCategoryId(categoryId);

        RAGSearchResultVO result = search(queryDTO);

        return StrUtil.isNotBlank(result.getAnswer()) ? result.getAnswer() : "抱歉，我暂时无法找到相关信息来回答您的问题。";
    }

    @Override
    public List<RAGSearchResultVO.RetrievedChunk> hybridSearch(String query, Integer topK, Long categoryId, Double similarityThreshold) {
        log.info("【混合检索】开始多路召回，查询: '{}', topK: {}", query, topK);

        List<KnowledgeChunkVO> vectorResults = Collections.emptyList();
        List<KnowledgeChunkVO> keywordResults = Collections.emptyList();

        // 1. 向量检索
        log.info("【混合检索】开始向量检索...");
        long vectorStart = System.currentTimeMillis();
        float[] queryEmbedding = embeddingService.embed(query);
        if (queryEmbedding != null && queryEmbedding.length > 0) {
            log.info("【混合检索】查询向量化完成，维度: {}，耗时: {}ms",
                    queryEmbedding.length, System.currentTimeMillis() - vectorStart);
            vectorResults = chunkService.vectorSearch(queryEmbedding, topK * 2, categoryId, similarityThreshold);
            log.info("【混合检索】向量检索完成，获取 {} 个结果", vectorResults.size());

            // 打印向量检索Top结果
            for (int i = 0; i < Math.min(vectorResults.size(), topK); i++) {
                KnowledgeChunkVO chunk = vectorResults.get(i);
                log.info("【混合检索】向量结果 #{} - 文档: '{}', 相似度: {:.4f}, 内容预览: {}",
                        i + 1,
                        chunk.getDocumentTitle(),
                        chunk.getSimilarity(),
                        chunk.getContent().substring(0, Math.min(80, chunk.getContent().length())) + "..."
                );
            }
        } else {
            log.warn("【混合检索】查询向量化失败");
        }

        // 2. 关键词检索
        log.info("【混合检索】开始关键词检索...");
        long keywordStart = System.currentTimeMillis();
        keywordResults = chunkService.keywordSearch(query, topK * 2, categoryId);
        log.info("【混合检索】关键词检索完成，获取 {} 个结果，耗时: {}ms",
                keywordResults.size(), System.currentTimeMillis() - keywordStart);

        // 打印关键词检索Top结果
        for (int i = 0; i < Math.min(keywordResults.size(), topK); i++) {
            KnowledgeChunkVO chunk = keywordResults.get(i);
            log.info("【混合检索】关键词结果 #{} - 文档: '{}', 内容预览: {}",
                    i + 1,
                    chunk.getDocumentTitle(),
                    chunk.getContent().substring(0, Math.min(80, chunk.getContent().length())) + "..."
            );
        }

        // 3. RRF融合排序
        log.info("【混合检索】开始RRF融合排序...");
        Map<Long, RAGSearchResultVO.RetrievedChunk> mergedResults = new LinkedHashMap<>();

        for (int i = 0; i < vectorResults.size(); i++) {
            KnowledgeChunkVO chunk = vectorResults.get(i);
            RAGSearchResultVO.RetrievedChunk retrievedChunk = toRetrievedChunk(chunk, 1);
            double rrfScore = 1.0 / (RRF_K + i + 1);
            retrievedChunk.setSimilarity(chunk.getSimilarity());
            mergedResults.put(chunk.getId(), retrievedChunk);
            log.debug("【混合检索】向量结果 RRF计算: chunkId={}, 排名={}, RRF分数={:.6f}",
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
                log.debug("【混合检索】关键词结果融合: chunkId={}, 原分数={:.6f}, 新增RRF={:.6f}, 最终分数={:.6f}",
                        chunk.getId(), existingScore, rrfScore, existingScore + rrfScore);
            } else {
                RAGSearchResultVO.RetrievedChunk retrievedChunk = toRetrievedChunk(chunk, 2);
                retrievedChunk.setSimilarity(rrfScore);
                mergedResults.put(chunk.getId(), retrievedChunk);
                log.debug("【混合检索】关键词结果添加: chunkId={}, RRF分数={:.6f}",
                        chunk.getId(), rrfScore);
            }
        }

        List<RAGSearchResultVO.RetrievedChunk> finalResults = mergedResults.values().stream()
                .sorted(Comparator.comparingDouble(RAGSearchResultVO.RetrievedChunk::getSimilarity).reversed())
                .limit(topK)
                .collect(Collectors.toList());

        log.info("【混合检索】RRF融合完成，最终返回 {} 个结果", finalResults.size());

        // 打印最终Top-K结果
        log.info("【混合检索】========== Top-{} 匹配结果 ==========", finalResults.size());
        for (int i = 0; i < finalResults.size(); i++) {
            RAGSearchResultVO.RetrievedChunk chunk = finalResults.get(i);
            String sourceType = chunk.getSource() == 1 ? "向量" : (chunk.getSource() == 2 ? "关键词" : "混合");
            log.info("【混合检索】Top-{} [来源: {}] 文档: '{}'", i + 1, sourceType, chunk.getDocumentTitle());
            log.info("【混合检索】Top-{} 相似度分数: {:.6f}", i + 1, chunk.getSimilarity());
            log.info("【混合检索】Top-{} 匹配文本: {}", i + 1, chunk.getContent());
            log.info("【混合检索】----------------------------------------");
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
            context.append("【文档").append(i + 1).append("】");
            if (StrUtil.isNotBlank(chunk.getDocumentTitle())) {
                context.append(" 来源: ").append(chunk.getDocumentTitle());
            }
            context.append("\n");
            context.append(chunk.getContent()).append("\n\n");
        }
        return context.toString();
    }

    private String generateAnswer(String query, String context) {
        String prompt = buildRAGPrompt(query, context);
        return aiService.chat(prompt);
    }

    private String buildRAGPrompt(String query, String context) {
        return String.format("""
                你是健身房的智能助手"健小助"，请根据以下知识库内容回答用户问题。

                要求：
                1. 只使用提供的知识库内容回答问题，不要编造信息
                2. 如果知识库中没有相关信息，请明确告知用户
                3. 回答要简洁、准确、友好
                4. 如果涉及具体数据（如价格、时间等），请准确引用

                知识库内容：
                %s

                用户问题：%s

                请回答：
                """, context, query);
    }
}
