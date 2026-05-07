package com.fitness.modules.chat.service;

import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.chat.agent.JianXiaoZhuAgentService;
import com.fitness.modules.chat.mapper.ChatMessageMapper;
import com.fitness.modules.chat.mapper.ChatSessionMapper;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.entity.ChatSession;
import com.fitness.modules.chat.prompt.ChatPromptTemplates;
import com.fitness.modules.chat.service.impl.ChatAssistantServiceImpl;
import com.fitness.modules.course.service.CourseService;
import com.fitness.modules.equipment.service.EquipmentService;
import com.fitness.modules.knowledge.service.RAGService;
import com.fitness.modules.plan.mapper.FitnessPlanDetailMapper;
import com.fitness.modules.plan.mapper.FitnessPlanMapper;
import com.fitness.modules.user.service.UserFitnessProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
}
