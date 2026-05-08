package com.fitness.modules.chat.service;

import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.chat.agent.JianXiaoZhuAgentService;
import com.fitness.modules.chat.mapper.ChatMessageMapper;
import com.fitness.modules.chat.mapper.ChatSessionMapper;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.entity.ChatMessage;
import com.fitness.modules.chat.model.entity.ChatSession;
import com.fitness.modules.chat.model.vo.ChatStreamEventVO;
import com.fitness.modules.chat.prompt.ChatPromptTemplates;
import com.fitness.modules.chat.service.impl.ChatAssistantServiceImpl;
import com.fitness.modules.course.service.CourseService;
import com.fitness.modules.equipment.service.EquipmentService;
import com.fitness.modules.knowledge.service.RAGService;
import com.fitness.modules.plan.mapper.FitnessPlanDetailMapper;
import com.fitness.modules.plan.mapper.FitnessPlanMapper;
import com.fitness.modules.user.service.UserFitnessProfileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatAssistantServiceImplTest {

    @Mock
    private ChatSessionMapper chatSessionMapper;
    @Mock
    private ChatMessageMapper chatMessageMapper;
    @Mock
    private ChatContextService chatContextService;
    @Mock
    private AIService aiService;
    @Mock
    private ChatPromptTemplates chatPromptTemplates;
    @Mock
    private RAGService ragService;
    @Mock
    private UserFitnessProfileService userFitnessProfileService;
    @Mock
    private FitnessPlanMapper fitnessPlanMapper;
    @Mock
    private FitnessPlanDetailMapper fitnessPlanDetailMapper;
    @Mock
    private CourseService courseService;
    @Mock
    private EquipmentService equipmentService;
    @Mock
    private JianXiaoZhuAgentService jianXiaoZhuAgentService;

    @InjectMocks
    private ChatAssistantServiceImpl service;

    @Test
    void sendMessageShouldDelegateToReactAgent() {
        ChatSession session = new ChatSession();
        session.setId(1L);
        session.setUserId(1L);
        when(chatSessionMapper.selectById(1L)).thenReturn(session);
        when(jianXiaoZhuAgentService.chat(anyLong(), anyLong(), anyString())).thenReturn("会员卡列表");

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSessionId(1L);
        dto.setContent("有哪些会员卡");

        service.sendMessage(1L, dto);

        verify(jianXiaoZhuAgentService).chat(1L, 1L, "有哪些会员卡");
    }

    @Test
    void sendMessageStreamShouldPersistConcatenatedDeltaContent() {
        ChatSession session = new ChatSession();
        session.setId(1L);
        session.setUserId(1L);
        session.setTitle("test");
        when(chatSessionMapper.selectById(1L)).thenReturn(session);
        when(jianXiaoZhuAgentService.streamChat(anyLong(), anyLong(), anyString())).thenReturn(Flux.just(
                ChatStreamEventVO.status("查询中"),
                ChatStreamEventVO.delta("会员卡"),
                ChatStreamEventVO.delta("列表")
        ));

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSessionId(1L);
        dto.setContent("有哪些会员卡");

        service.sendMessageStream(1L, dto).collectList().block();

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatContextService, atLeastOnce()).saveMessageToDatabase(captor.capture());

        ChatMessage assistantMessage = captor.getAllValues().stream()
                .filter(message -> "assistant".equals(message.getRole()))
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals("会员卡列表", assistantMessage.getContent());
    }
}
