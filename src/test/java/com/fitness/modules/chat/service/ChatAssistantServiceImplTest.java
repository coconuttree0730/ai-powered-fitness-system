package com.fitness.modules.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.chat.agent.JianXiaoZhuAgentService;
import com.fitness.modules.chat.mapper.ChatMessageMapper;
import com.fitness.modules.chat.mapper.ChatSessionMapper;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.entity.ChatMessage;
import com.fitness.modules.chat.model.entity.ChatSession;
import com.fitness.modules.chat.model.vo.ChatStreamEventVO;
import com.fitness.modules.chat.model.vo.FitnessPlanCardVO;
import com.fitness.modules.chat.service.impl.ChatAssistantServiceImpl;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.service.CourseService;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.service.EquipmentService;
import com.fitness.modules.knowledge.service.RAGService;
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
import static org.mockito.ArgumentMatchers.anyInt;
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
        when(courseService.getHomePageCourseCards(anyInt())).thenReturn(List.of(buildCourse()));
        when(equipmentService.getEquipmentList(any())).thenReturn(new Page<EquipmentVO>().setRecords(List.of(buildEquipment())));
        when(aiService.generateFitnessPlanFromProfile(any())).thenReturn(buildAiPlanResponse());

        FitnessPlanCardVO result = service.generateFitnessPlanCard(1L, "MUSCLE_GAIN", "CHEST", "BEGINNER");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(7, result.getDuration());
        Assertions.assertEquals(1, result.getWeeklyPlan().size());
        Assertions.assertEquals("Monday", result.getWeeklyPlan().get(0).getDayName());
        Assertions.assertEquals("Bench Press", result.getWeeklyPlan().get(0).getExercises().get(0).getName());
        Assertions.assertEquals("Catalog Course", result.getRecommendedCourses().get(0).getCourseName());
        Assertions.assertEquals("Treadmill", result.getRecommendedEquipment().get(0).getEquipmentName());
        verify(aiService, never()).generateFitnessPlan(anyString(), anyString(), anyString(), any(), any(), any());
    }

    @Test
    void generateFitnessPlanCardShouldFallbackToLegacyTextParsingWhenStructuredAiResponseIsMissing() {
        when(userFitnessProfileService.isProfileComplete(1L)).thenReturn(true);
        when(userFitnessProfileService.getProfile(1L)).thenReturn(buildProfile());
        when(courseService.getHomePageCourseCards(anyInt())).thenReturn(List.of(buildCourse()));
        when(equipmentService.getEquipmentList(any())).thenReturn(new Page<EquipmentVO>().setRecords(List.of(buildEquipment())));
        when(aiService.generateFitnessPlanFromProfile(any())).thenReturn(null);
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

    private CourseCardVO buildCourse() {
        CourseCardVO course = new CourseCardVO();
        course.setId(1L);
        course.setName("Catalog Course");
        course.setCategory("strength");
        course.setLevel("BEGINNER");
        course.setDuration(45);
        course.setImage("course.png");
        course.setDesc("Course description");
        return course;
    }

    private EquipmentVO buildEquipment() {
        EquipmentVO equipment = new EquipmentVO();
        equipment.setId(2L);
        equipment.setEquipmentName("Treadmill");
        equipment.setTypeName("Cardio");
        equipment.setLocation("A-01");
        equipment.setImageUrl("equipment.png");
        equipment.setDescription("Equipment description");
        return equipment;
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
}
