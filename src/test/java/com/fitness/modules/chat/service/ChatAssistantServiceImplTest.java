package com.fitness.modules.chat.service;

import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.chat.agent.JianXiaoZhuAgentService;
import com.fitness.modules.chat.mapper.ChatMessageMapper;
import com.fitness.modules.chat.mapper.ChatSessionMapper;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.entity.ChatMessage;
import com.fitness.modules.chat.model.entity.ChatSession;
import com.fitness.modules.chat.model.vo.ChatMessageVO;
import com.fitness.modules.chat.model.vo.ChatSessionVO;
import com.fitness.modules.chat.model.vo.ChatStreamEventVO;
import com.fitness.modules.chat.model.vo.DayPlanVO;
import com.fitness.modules.chat.model.vo.FitnessPlanCardVO;
import com.fitness.modules.chat.service.impl.ChatAssistantServiceImpl;
import com.fitness.modules.chat.service.impl.FitnessPlanParser;
import com.fitness.modules.chat.util.ExerciseJsonSerializer;
import com.fitness.modules.plan.mapper.FitnessPlanDetailMapper;
import com.fitness.modules.plan.mapper.FitnessPlanMapper;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;
import com.fitness.modules.user.service.UserFitnessProfileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
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
    private UserFitnessProfileService userFitnessProfileService;
    @Mock
    private FitnessPlanMapper fitnessPlanMapper;
    @Mock
    private FitnessPlanDetailMapper fitnessPlanDetailMapper;
    @Mock
    private JianXiaoZhuAgentService jianXiaoZhuAgentService;
    @Mock
    private RecommendationService recommendationService;
    @Mock
    private FitnessPlanParser fitnessPlanParser;
    @Mock
    private ExerciseJsonSerializer exerciseJsonSerializer;

    @InjectMocks
    private ChatAssistantServiceImpl service;

    @Test
    void sendMessageShouldDelegateToReactAgent() {
        ChatSession session = new ChatSession();
        session.setId(1L);
        session.setUserId(1L);
        when(chatSessionMapper.selectById(1L)).thenReturn(session);
        when(jianXiaoZhuAgentService.chat(anyLong(), anyLong(), anyString())).thenReturn("membership card list");

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSessionId(1L);
        dto.setContent("what membership cards are available");

        service.sendMessage(1L, dto);

        verify(jianXiaoZhuAgentService).chat(1L, 1L, "what membership cards are available");
    }

    @Test
    void sendMessageStreamShouldPersistConcatenatedDeltaContent() {
        ChatSession session = new ChatSession();
        session.setId(1L);
        session.setUserId(1L);
        session.setTitle("test");
        when(chatSessionMapper.selectById(1L)).thenReturn(session);
        when(jianXiaoZhuAgentService.streamChat(anyLong(), anyLong(), anyString())).thenReturn(Flux.just(
                ChatStreamEventVO.status("querying"),
                ChatStreamEventVO.delta("membership "),
                ChatStreamEventVO.delta("cards")
        ));

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSessionId(1L);
        dto.setContent("what membership cards are available");

        service.sendMessageStream(1L, dto).collectList().block();

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatContextService, atLeastOnce()).saveMessageToDatabase(captor.capture());

        ChatMessage assistantMessage = captor.getAllValues().stream()
                .filter(message -> "assistant".equals(message.getRole()))
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals("membership cards", assistantMessage.getContent());
    }

    @Test
    void generateFitnessPlanCardShouldPreferStructuredAiResponse() {
        when(userFitnessProfileService.isProfileComplete(1L)).thenReturn(true);
        when(userFitnessProfileService.getProfile(1L)).thenReturn(buildProfile());
        when(recommendationService.buildAvailableCourseCatalog()).thenReturn("[courses]");
        when(recommendationService.buildAvailableEquipmentCatalog()).thenReturn("[equipment]");
        when(aiService.generateFitnessPlanFromProfile(any())).thenReturn(buildAiPlanResponse());

        FitnessPlanCardVO result = service.generateFitnessPlanCard(1L, "MUSCLE_GAIN", "CHEST", "BEGINNER");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(7, result.getDuration());
        Assertions.assertEquals(1, result.getWeeklyPlan().size());
        Assertions.assertEquals("Monday", result.getWeeklyPlan().get(0).getDayName());
        Assertions.assertEquals("Bench Press", result.getWeeklyPlan().get(0).getExercises().get(0).getName());
        verify(aiService, never()).generateFitnessPlan(anyString(), anyString(), anyString(), any(), any(), any());
    }

    @Test
    void generateFitnessPlanCardShouldFallbackToLegacyTextParsingWhenStructuredAiResponseIsMissing() {
        when(userFitnessProfileService.isProfileComplete(1L)).thenReturn(true);
        when(userFitnessProfileService.getProfile(1L)).thenReturn(buildProfile());
        when(recommendationService.buildAvailableCourseCatalog()).thenReturn("[courses]");
        when(recommendationService.buildAvailableEquipmentCatalog()).thenReturn("[equipment]");
        when(aiService.generateFitnessPlanFromProfile(any())).thenReturn(null);
        when(fitnessPlanParser.parseWeeklyPlan("No structured weekly plan")).thenReturn(generateDefaultWeeklyPlan());
        when(aiService.generateFitnessPlan(anyString(), anyString(), anyString(), any(), any(), any()))
                .thenReturn("No structured weekly plan");

        FitnessPlanCardVO result = service.generateFitnessPlanCard(1L, "MUSCLE_GAIN", "CHEST", "BEGINNER");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(7, result.getWeeklyPlan().size());
        verify(aiService).generateFitnessPlan(anyString(), anyString(), anyString(), any(), any(), any());
    }

    private UserFitnessProfileVO buildProfile() {
        UserFitnessProfileVO profile = new UserFitnessProfileVO();
        profile.setHeight(new BigDecimal("175"));
        profile.setWeight(new BigDecimal("70"));
        profile.setAge(28);
        profile.setExperience("BEGINNER");
        profile.setFitnessGoal("MUSCLE_GAIN");
        return profile;
    }

    private FitnessPlanResponseDTO buildAiPlanResponse() {
        FitnessPlanResponseDTO response = new FitnessPlanResponseDTO();
        response.setSubtitle("AI plan");

        FitnessPlanResponseDTO.ExerciseDTO exercise = new FitnessPlanResponseDTO.ExerciseDTO();
        exercise.setName("Bench Press");
        exercise.setSets(4);
        exercise.setReps(10);
        exercise.setRestSeconds(90);

        FitnessPlanResponseDTO.DayPlanDTO dayPlan = new FitnessPlanResponseDTO.DayPlanDTO();
        dayPlan.setDayName("Monday");
        dayPlan.setFocus("Chest");
        dayPlan.setExercises(List.of(exercise));

        response.setWeeklyPlan(List.of(dayPlan));
        return response;
    }

    private List<DayPlanVO> generateDefaultWeeklyPlan() {
        List<DayPlanVO> weeklyPlan = new java.util.ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            DayPlanVO dayPlan = new DayPlanVO();
            dayPlan.setDayOfWeek(i);
            dayPlan.setDayName(new String[]{"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"}[i]);
            dayPlan.setExercises(new java.util.ArrayList<>());
            weeklyPlan.add(dayPlan);
        }
        return weeklyPlan;
    }
}