package com.fitness.modules.ai.evaluation.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.modules.ai.evaluation.model.dto.AIEvaluationRunDTO;
import com.fitness.modules.ai.evaluation.model.vo.AIEvaluationResultVO;
import com.fitness.modules.knowledge.model.dto.RAGDebugQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGDebugChunkVO;
import com.fitness.modules.knowledge.model.vo.RAGDebugResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AIEvaluationServiceImplTest {

    @Test
    void runShouldScoreSelectedCaseByMatchedDocumentOrKeyword() {
        RAGService ragService = mock(RAGService.class);
        RAGDebugChunkVO chunk = new RAGDebugChunkVO();
        chunk.setDocumentTitle("会员服务规则");
        chunk.setContent("会员卡仅限本人使用，不得转借或转让。");
        RAGDebugResultVO debugResult = new RAGDebugResultVO();
        debugResult.setMergedChunks(List.of(chunk));
        debugResult.setRetrievalTimeMs(12L);
        when(ragService.debugSearch(any())).thenReturn(debugResult);

        AIEvaluationServiceImpl service = new AIEvaluationServiceImpl(ragService, new ObjectMapper());
        AIEvaluationRunDTO runDTO = new AIEvaluationRunDTO();
        runDTO.setCaseIds(List.of("RAG-001"));

        AIEvaluationResultVO result = service.run(runDTO);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getPassed());
        assertEquals(1.0, result.getHitRate());
        assertTrue(result.getCases().get(0).getMatchedDocument());
        assertTrue(result.getCases().get(0).getMatchedKeyword());
        ArgumentCaptor<RAGDebugQueryDTO> queryCaptor = ArgumentCaptor.forClass(RAGDebugQueryDTO.class);
        verify(ragService).debugSearch(queryCaptor.capture());
        assertEquals("membership", queryCaptor.getValue().getCategoryCode());
    }
}
