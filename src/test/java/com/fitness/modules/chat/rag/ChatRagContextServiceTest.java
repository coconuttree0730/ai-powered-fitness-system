package com.fitness.modules.chat.rag;

import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRagContextServiceTest {

    @Mock
    private RAGService ragService;

    @InjectMocks
    private ChatRagContextService service;

    @Test
    void buildContextShouldUseTopKFive() {
        when(ragService.search(any())).thenReturn(new RAGSearchResultVO());

        service.buildContext("营业时间");

        ArgumentCaptor<RAGQueryDTO> captor = ArgumentCaptor.forClass(RAGQueryDTO.class);
        verify(ragService).search(captor.capture());
        assertEquals(5, captor.getValue().getTopK());
    }
}
