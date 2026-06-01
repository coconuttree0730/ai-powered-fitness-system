package com.fitness.modules.knowledge.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.knowledge.model.dto.RAGDebugQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGDebugResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RAGAdminControllerTest {

    @Test
    void debugSearchShouldReturnWrappedServiceResult() {
        RAGService ragService = mock(RAGService.class);
        RAGAdminController controller = new RAGAdminController(ragService);
        RAGDebugQueryDTO queryDTO = new RAGDebugQueryDTO();
        queryDTO.setQuery("会员卡转让");
        RAGDebugResultVO debugResult = new RAGDebugResultVO();
        when(ragService.debugSearch(queryDTO)).thenReturn(debugResult);

        Result<RAGDebugResultVO> result = controller.debugSearch(queryDTO);

        assertSame(debugResult, result.getData());
    }
}
