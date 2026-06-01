package com.fitness.modules.knowledge.service.impl;

import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.chat.prompt.ChatPromptTemplates;
import com.fitness.modules.knowledge.mapper.KnowledgeCategoryMapper;
import com.fitness.modules.knowledge.model.dto.RAGDebugQueryDTO;
import com.fitness.modules.knowledge.model.entity.KnowledgeCategory;
import com.fitness.modules.knowledge.model.vo.KnowledgeChunkVO;
import com.fitness.modules.knowledge.model.vo.RAGDebugResultVO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.EmbeddingService;
import com.fitness.modules.knowledge.service.KnowledgeChunkService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RAGServiceImplTest {

    @Test
    void hybridSearchShouldKeepRawScoresAndFinalScoreSeparate() {
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        KnowledgeChunkService chunkService = mock(KnowledgeChunkService.class);
        KnowledgeCategoryMapper categoryMapper = mock(KnowledgeCategoryMapper.class);
        AIService aiService = mock(AIService.class);
        ChatPromptTemplates chatPromptTemplates = mock(ChatPromptTemplates.class);

        when(embeddingService.embed("会员卡转让")).thenReturn(new float[] {0.1f, 0.2f});
        when(chunkService.vectorSearch(any(), eq(4), eq(10L), eq(0.7)))
                .thenReturn(List.of(chunk(1L, "会员规则", "会员卡不得转让", 0.82)));
        when(chunkService.keywordSearch("会员卡转让", 4, 10L))
                .thenReturn(List.of(chunk(1L, "会员规则", "会员卡不得转让", 0.45)));

        RAGServiceImpl service = new RAGServiceImpl(
                embeddingService,
                chunkService,
                categoryMapper,
                aiService,
                chatPromptTemplates,
                Runnable::run);

        List<RAGSearchResultVO.RetrievedChunk> results =
                service.hybridSearch("会员卡转让", 2, 10L, 0.7);

        RAGSearchResultVO.RetrievedChunk chunk = results.get(0);
        assertEquals(0.82, chunk.getVectorSimilarity());
        assertEquals(0.45, chunk.getKeywordScore());
        assertNotNull(chunk.getRrfScore());
        assertEquals(chunk.getRrfScore(), chunk.getFinalScore());
        assertEquals(chunk.getFinalScore(), chunk.getSimilarity());
        assertEquals(3, chunk.getSource());
    }

    @Test
    void debugSearchShouldReturnRawAndMergedResultsWithoutGeneratingAnswer() {
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        KnowledgeChunkService chunkService = mock(KnowledgeChunkService.class);
        KnowledgeCategoryMapper categoryMapper = mock(KnowledgeCategoryMapper.class);
        AIService aiService = mock(AIService.class);
        ChatPromptTemplates chatPromptTemplates = mock(ChatPromptTemplates.class);

        when(embeddingService.embed("会员卡转让")).thenReturn(new float[] {0.1f, 0.2f});
        when(chunkService.vectorSearch(any(), eq(4), eq(10L), eq(0.7)))
                .thenReturn(List.of(chunk(1L, "会员规则", "会员卡不得转让", 0.82)));
        when(chunkService.keywordSearch("会员卡转让", 4, 10L))
                .thenReturn(List.of(chunk(1L, "会员规则", "会员卡不得转让", 0.45)));

        RAGServiceImpl service = new RAGServiceImpl(
                embeddingService,
                chunkService,
                categoryMapper,
                aiService,
                chatPromptTemplates,
                Runnable::run);
        RAGDebugQueryDTO queryDTO = new RAGDebugQueryDTO();
        queryDTO.setQuery("会员卡转让");
        queryDTO.setTopK(2);
        queryDTO.setCategoryId(10L);
        queryDTO.setSimilarityThreshold(0.7);

        RAGDebugResultVO result = service.debugSearch(queryDTO);

        assertEquals(1, result.getVectorChunks().size());
        assertEquals(1, result.getKeywordChunks().size());
        assertEquals(1, result.getMergedChunks().size());
        assertEquals(0.82, result.getVectorChunks().get(0).getVectorSimilarity());
        assertEquals(0.45, result.getKeywordChunks().get(0).getKeywordScore());
        assertEquals(result.getMergedChunks().get(0).getRrfScore(),
                result.getMergedChunks().get(0).getFinalScore());
        verify(aiService, never()).chat(any(), any());
    }

    @Test
    void debugSearchShouldResolveCategoryCodeBeforeSearching() {
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        KnowledgeChunkService chunkService = mock(KnowledgeChunkService.class);
        KnowledgeCategoryMapper categoryMapper = mock(KnowledgeCategoryMapper.class);
        AIService aiService = mock(AIService.class);
        ChatPromptTemplates chatPromptTemplates = mock(ChatPromptTemplates.class);
        KnowledgeCategory category = new KnowledgeCategory();
        category.setId(10L);
        category.setCode("MEMBERSHIP");

        when(categoryMapper.selectList(any())).thenReturn(List.of(category));
        when(embeddingService.embed("会员卡转让")).thenReturn(new float[] {0.1f, 0.2f});
        when(chunkService.vectorSearch(any(), eq(4), eq(10L), eq(0.7)))
                .thenReturn(List.of(chunk(1L, "会员规则", "会员卡不得转让", 0.82)));
        when(chunkService.keywordSearch("会员卡转让", 4, 10L))
                .thenReturn(List.of(chunk(1L, "会员规则", "会员卡不得转让", 0.45)));

        RAGServiceImpl service = new RAGServiceImpl(
                embeddingService,
                chunkService,
                categoryMapper,
                aiService,
                chatPromptTemplates,
                Runnable::run);
        RAGDebugQueryDTO queryDTO = new RAGDebugQueryDTO();
        queryDTO.setQuery("会员卡转让");
        queryDTO.setTopK(2);
        queryDTO.setCategoryCode("MEMBERSHIP");
        queryDTO.setSimilarityThreshold(0.7);

        RAGDebugResultVO result = service.debugSearch(queryDTO);

        assertEquals(10L, result.getCategoryId());
        assertEquals("MEMBERSHIP", result.getCategoryCode());
        verify(chunkService).vectorSearch(any(), eq(4), eq(10L), eq(0.7));
        verify(chunkService).keywordSearch("会员卡转让", 4, 10L);
    }

    private KnowledgeChunkVO chunk(Long id, String title, String content, Double similarity) {
        KnowledgeChunkVO chunk = new KnowledgeChunkVO();
        chunk.setId(id);
        chunk.setDocumentId(100L + id);
        chunk.setDocumentTitle(title);
        chunk.setContent(content);
        chunk.setSimilarity(similarity);
        return chunk;
    }
}
