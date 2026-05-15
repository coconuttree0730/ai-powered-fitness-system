package com.fitness.modules.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
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
import com.fitness.modules.chat.model.vo.ExerciseVO;
import com.fitness.modules.chat.model.vo.FitnessPlanCardVO;
import com.fitness.modules.chat.service.ChatAssistantService;
import com.fitness.modules.chat.service.ChatContextService;
import com.fitness.modules.chat.service.RecommendationService;
import com.fitness.modules.chat.util.ExerciseJsonSerializer;
import com.fitness.modules.plan.mapper.FitnessPlanDetailMapper;
import com.fitness.modules.plan.mapper.FitnessPlanMapper;
import com.fitness.modules.plan.model.entity.FitnessPlan;
import com.fitness.modules.plan.model.entity.FitnessPlanDetail;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;
import com.fitness.modules.user.service.UserFitnessProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAssistantServiceImpl implements ChatAssistantService {

    private static final String DEFAULT_SESSION_TITLE = "新对话";
    private static final String TRAINING_TYPE_REST = "休息恢复";
    private static final String TRAINING_TYPE_STRENGTH = "力量训练";

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatContextService chatContextService;
    private final AIService aiService;
    private final UserFitnessProfileService userFitnessProfileService;
    private final FitnessPlanMapper fitnessPlanMapper;
    private final FitnessPlanDetailMapper fitnessPlanDetailMapper;
    private final RecommendationService recommendationService;
    private final JianXiaoZhuAgentService jianXiaoZhuAgentService;
    private final FitnessPlanParser fitnessPlanParser;
    private final ExerciseJsonSerializer exerciseJsonSerializer;

    @Override
    @Transactional
    public ChatSessionVO createSession(Long userId) {
        ChatSession session = createSessionEntity(userId);
        log.info("Created chat session: sessionId={}, userId={}", session.getId(), userId);
        return convertToSessionVO(session);
    }

    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long userId, ChatMessageDTO dto) {
        ChatSession session = getOrCreateSession(userId, dto.getSessionId());

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        userMessage.setCreateTime(LocalDateTime.now());

        chatContextService.saveMessageToDatabase(userMessage);
        chatContextService.addMessage(session.getId(), userMessage);

        String aiResponse = jianXiaoZhuAgentService.chat(userId, session.getId(), dto.getContent());

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(aiResponse);
        assistantMessage.setCreateTime(LocalDateTime.now());

        chatContextService.saveMessageToDatabase(assistantMessage);
        chatContextService.addMessage(session.getId(), assistantMessage);

        updateSessionTitleIfNeeded(session, dto.getContent());

        log.info("Sent message: sessionId={}, userId={}, contentLength={}",
                session.getId(), userId, dto.getContent().length());
        return convertToMessageVO(assistantMessage);
    }

    @Override
    public Flux<ChatStreamEventVO> sendMessageStream(Long userId, ChatMessageDTO dto) {
        ChatSession session;
        if (dto.getSessionId() == null) {
            session = createSessionEntity(userId);
        } else {
            session = chatSessionMapper.selectById(dto.getSessionId());
            if (session == null || !session.getUserId().equals(userId)) {
                return Flux.error(new BusinessException("会话不存在或无权访问"));
            }
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        userMessage.setCreateTime(LocalDateTime.now());

        chatContextService.saveMessageToDatabase(userMessage);
        chatContextService.addMessage(session.getId(), userMessage);

        StringBuilder fullResponse = new StringBuilder();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        return jianXiaoZhuAgentService.streamChat(userId, session.getId(), dto.getContent())
                .doOnNext(event -> {
                    if ("delta".equals(event.getType()) && StrUtil.isNotBlank(event.getText())) {
                        fullResponse.append(event.getText());
                    }
                })
                .doOnComplete(() -> {
                    SecurityContextHolder.setContext(securityContext);
                    try {
                        saveAssistantMessage(session, fullResponse.toString(), dto.getContent());
                        log.info("Completed stream message: sessionId={}, userId={}", session.getId(), userId);
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                })
                .doOnError(error -> log.error("Stream message failed: sessionId={}, userId={}, error={}",
                        session.getId(), userId, error.getMessage()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAssistantMessage(ChatSession session, String content, String firstMessage) {
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(content);
        assistantMessage.setCreateTime(LocalDateTime.now());

        chatContextService.saveMessageToDatabase(assistantMessage);
        chatContextService.addMessage(session.getId(), assistantMessage);
        updateSessionTitleIfNeeded(session, firstMessage);
    }

    @Override
    public List<ChatSessionVO> getUserSessions(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getDeleted, false)
                        .orderByDesc(ChatSession::getUpdateTime)
        );

        return sessions.stream()
                .map(this::convertToSessionVO)
                .collect(Collectors.toList());
    }

    @Override
    public ChatSessionVO getSessionDetail(Long sessionId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在或无权访问");
        }
        return convertToSessionVO(session);
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在或无权访问");
        }

        session.setDeleted(true);
        chatSessionMapper.updateById(session);
        chatContextService.clearContext(sessionId);

        log.info("Deleted chat session: sessionId={}, userId={}", sessionId, userId);
    }

    @Override
    public List<ChatMessageVO> getSessionMessages(Long sessionId, Long userId, Long lastMessageId, Integer limit) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在或无权访问");
        }

        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last("LIMIT " + safeLimit);

        if (lastMessageId != null) {
            ChatMessage lastMessage = chatMessageMapper.selectById(lastMessageId);
            if (lastMessage != null) {
                wrapper.lt(ChatMessage::getCreateTime, lastMessage.getCreateTime());
            }
        }

        return chatMessageMapper.selectList(wrapper).stream()
                .map(this::convertToMessageVO)
                .collect(Collectors.toList());
    }

    @Override
    public FitnessPlanCardVO generateFitnessPlanCard(Long userId, String goal, String bodyPart, String experience) {
        if (!userFitnessProfileService.isProfileComplete(userId)) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETE);
        }

        UserFitnessProfileVO profile = userFitnessProfileService.getProfile(userId);

        FitnessPlanCardVO planCard = new FitnessPlanCardVO();
        planCard.setPlanName(generatePlanName(goal, bodyPart));
        planCard.setGoal(goal);
        planCard.setBodyPart(bodyPart);
        planCard.setExperience(experience);
        planCard.setDuration(7);
        planCard.setWeeklyPlan(generateWeeklyPlanForCard(userId, profile, goal, bodyPart, experience));
        planCard.setWeeklyAdvice(recommendationService.generateWeeklyAdvice(goal, experience));
        planCard.setRecommendedCourses(recommendationService.recommendCourses(goal, bodyPart));
        planCard.setRecommendedEquipment(recommendationService.recommendEquipment(bodyPart));

        log.info("Generated fitness plan card: userId={}, goal={}, bodyPart={}", userId, goal, bodyPart);
        return planCard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveFitnessPlan(Long userId, FitnessPlanCardVO planCard) {
        FitnessPlan plan = new FitnessPlan();
        plan.setUserId(userId);
        plan.setPlanName(planCard.getPlanName());
        plan.setGoal(planCard.getGoal());
        plan.setDuration(planCard.getDuration());
        plan.setStatus(1);
        plan.setDeleted(false);

        fitnessPlanMapper.insert(plan);

        if (CollUtil.isNotEmpty(planCard.getWeeklyPlan())) {
            int sortOrder = 0;
            for (DayPlanVO dayPlan : planCard.getWeeklyPlan()) {
                FitnessPlanDetail detail = new FitnessPlanDetail();
                detail.setPlanId(plan.getId());
                detail.setDayIndex(dayPlan.getDayOfWeek());
                detail.setDayName(dayPlan.getDayName());
                detail.setFocus(dayPlan.getFocus());
                detail.setDuration(dayPlan.getTotalDuration());
                detail.setNotes(dayPlan.getDailyTips());
                detail.setSortOrder(sortOrder++);
                detail.setDeleted(false);
                detail.setExercisesJson(exerciseJsonSerializer.serializeExercises(dayPlan.getExercises()));
                fitnessPlanDetailMapper.insert(detail);
            }
        }

        log.info("Saved fitness plan: userId={}, planId={}", userId, plan.getId());
        return plan.getId();
    }

    @Override
    public List<FitnessPlanCardVO> getUserPlans(Long userId) {
        List<FitnessPlan> plans = fitnessPlanMapper.selectByUserId(userId);
        List<FitnessPlanCardVO> result = new ArrayList<>();

        for (FitnessPlan plan : plans) {
            FitnessPlanCardVO vo = new FitnessPlanCardVO();
            vo.setPlanName(plan.getPlanName());
            vo.setGoal(plan.getGoal());
            vo.setDuration(plan.getDuration());
            vo.setWeeklyPlan(convertDetailsToWeeklyPlan(fitnessPlanDetailMapper.selectByPlanId(plan.getId())));
            result.add(vo);
        }

        return result;
    }

    private List<DayPlanVO> generateWeeklyPlanForCard(
            Long userId, UserFitnessProfileVO profile, String goal, String bodyPart, String experience) {
        try {
            FitnessPlanResponseDTO response = aiService.generateFitnessPlanFromProfile(
                    buildFitnessPlanProfileVariables(profile, goal, bodyPart, experience));
            if (response != null && CollUtil.isNotEmpty(response.getWeeklyPlan())) {
                return convertAiWeeklyPlan(response.getWeeklyPlan());
            }
            log.warn("Structured AI fitness plan is empty, fallback to legacy parsing: userId={}", userId);
        } catch (Exception e) {
            log.warn("Structured AI fitness plan failed, fallback to legacy parsing: userId={}, error={}",
                    userId, e.getMessage());
        }
        return generateLegacyWeeklyPlan(profile, goal, bodyPart, experience);
    }

    private Map<String, Object> buildFitnessPlanProfileVariables(
            UserFitnessProfileVO profile, String goal, String bodyPart, String experience) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("height", toInteger(profile.getHeight()));
        variables.put("weight", toInteger(profile.getWeight()));
        variables.put("age", profile.getAge());
        variables.put("gender", profile.getGender());
        variables.put("profileGoal", profile.getFitnessGoal());
        variables.put("profileExperience", profile.getExperience());
        variables.put("goal", goal);
        variables.put("experience", experience);
        variables.put("bodyPart", bodyPart);
        variables.put("availableCourses", recommendationService.buildAvailableCourseCatalog());
        variables.put("availableEquipment", recommendationService.buildAvailableEquipmentCatalog());
        return variables;
    }

    private List<DayPlanVO> convertAiWeeklyPlan(List<FitnessPlanResponseDTO.DayPlanDTO> weeklyPlan) {
        List<DayPlanVO> result = new ArrayList<>();
        for (int i = 0; i < weeklyPlan.size(); i++) {
            FitnessPlanResponseDTO.DayPlanDTO source = weeklyPlan.get(i);
            DayPlanVO target = new DayPlanVO();
            target.setDayOfWeek(i + 1);
            target.setDayName(source.getDayName());
            target.setFocus(source.getFocus());
            List<ExerciseVO> exercises = convertAiExercises(source);
            target.setExercises(exercises);
            target.setTrainingType(exercises.isEmpty() ? TRAINING_TYPE_REST : TRAINING_TYPE_STRENGTH);
            target.setTotalDuration(exercises.isEmpty() ? 0 : 60);
            result.add(target);
        }
        return result;
    }

    private List<ExerciseVO> convertAiExercises(FitnessPlanResponseDTO.DayPlanDTO dayPlan) {
        if (CollUtil.isEmpty(dayPlan.getExercises())) return new ArrayList<>();

        List<ExerciseVO> exercises = new ArrayList<>();
        for (FitnessPlanResponseDTO.ExerciseDTO source : dayPlan.getExercises()) {
            ExerciseVO target = new ExerciseVO();
            target.setName(source.getName());
            target.setSets(source.getSets());
            target.setReps(source.getReps());
            target.setRestTime(source.getRestSeconds());
            target.setTargetMuscle(dayPlan.getFocus());
            target.setDifficulty("MEDIUM");
            exercises.add(target);
        }
        return exercises;
    }

    private List<DayPlanVO> generateLegacyWeeklyPlan(
            UserFitnessProfileVO profile, String goal, String bodyPart, String experience) {
        try {
            String response = aiService.generateFitnessPlan(
                    goal, bodyPart, experience,
                    toInteger(profile.getHeight()), toInteger(profile.getWeight()), profile.getAge());
            if (StrUtil.isBlank(response)) {
                return fitnessPlanParser.generateDefaultWeeklyPlan();
            }
            return fitnessPlanParser.parseWeeklyPlan(response);
        } catch (Exception e) {
            log.error("Failed to generate legacy fitness plan: userId={}, error={}",
                    profile.getUserId(), e.getMessage());
            throw new BusinessException(ErrorCode.AI_GENERATE_ERROR);
        }
    }

    private List<DayPlanVO> convertDetailsToWeeklyPlan(List<FitnessPlanDetail> details) {
        Map<Integer, List<FitnessPlanDetail>> groupedByDay = details.stream()
                .collect(Collectors.groupingBy(FitnessPlanDetail::getDayIndex));

        List<DayPlanVO> weeklyPlan = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            DayPlanVO dayPlan = new DayPlanVO();
            dayPlan.setDayOfWeek(i);
            dayPlan.setDayName(fitnessPlanParser.getDayName(i));

            List<FitnessPlanDetail> dayDetails = groupedByDay.getOrDefault(i, new ArrayList<>());
            if (!dayDetails.isEmpty()) {
                FitnessPlanDetail firstDetail = dayDetails.get(0);
                dayPlan.setDayName(StrUtil.blankToDefault(firstDetail.getDayName(), fitnessPlanParser.getDayName(i)));
                dayPlan.setFocus(firstDetail.getFocus());
                dayPlan.setTotalDuration(firstDetail.getDuration());
                dayPlan.setDailyTips(firstDetail.getNotes());
            }

            List<ExerciseVO> exercises = new ArrayList<>();
            for (FitnessPlanDetail detail : dayDetails) {
                if (StrUtil.isNotBlank(detail.getExercisesJson())) {
                    exercises.addAll(exerciseJsonSerializer.parseExercises(detail.getExercisesJson()));
                }
            }

            dayPlan.setExercises(exercises);
            if (StrUtil.isBlank(dayPlan.getTrainingType())) {
                dayPlan.setTrainingType(exercises.isEmpty() ? TRAINING_TYPE_REST : TRAINING_TYPE_STRENGTH);
            }
            weeklyPlan.add(dayPlan);
        }
        return weeklyPlan;
    }

    private ChatSession getOrCreateSession(Long userId, Long sessionId) {
        if (sessionId == null) return createSessionEntity(userId);

        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在或无权访问");
        }
        return session;
    }

    private ChatSession createSessionEntity(Long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(DEFAULT_SESSION_TITLE);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        session.setDeleted(false);
        chatSessionMapper.insert(session);
        return session;
    }

    private void updateSessionTitleIfNeeded(ChatSession session, String firstMessage) {
        if (!DEFAULT_SESSION_TITLE.equals(session.getTitle())) return;
        session.setTitle(generateSessionTitle(firstMessage));
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);
    }

    private String generateSessionTitle(String firstMessage) {
        if (StrUtil.isBlank(firstMessage)) return DEFAULT_SESSION_TITLE;
        return firstMessage.length() <= 20 ? firstMessage : firstMessage.substring(0, 20) + "...";
    }

    private ChatSessionVO convertToSessionVO(ChatSession session) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setCreateTime(session.getCreateTime());
        vo.setUpdateTime(session.getUpdateTime());
        return vo;
    }

    private ChatMessageVO convertToMessageVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private String generatePlanName(String goal, String bodyPart) {
        return goal + "-" + bodyPart + "训练计划";
    }

    private Integer toInteger(Number value) {
        return value == null ? null : value.intValue();
    }
}