package com.fitness.modules.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.fitness.modules.chat.model.vo.CourseRecommendVO;
import com.fitness.modules.chat.model.vo.DayPlanVO;
import com.fitness.modules.chat.model.vo.EquipmentRecommendVO;
import com.fitness.modules.chat.model.vo.ExerciseVO;
import com.fitness.modules.chat.model.vo.FitnessPlanCardVO;
import com.fitness.modules.chat.model.vo.WeeklyAdviceVO;
import com.fitness.modules.chat.service.ChatAssistantService;
import com.fitness.modules.chat.service.ChatContextService;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.service.CourseService;
import com.fitness.modules.equipment.model.dto.EquipmentQueryDTO;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.service.EquipmentService;
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
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAssistantServiceImpl implements ChatAssistantService {

    private static final String DEFAULT_SESSION_TITLE = "新对话";
    private static final String[] DAY_NAMES = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final String TRAINING_TYPE_STRENGTH = "力量训练";
    private static final String TRAINING_TYPE_CARDIO = "有氧训练";
    private static final String TRAINING_TYPE_CORE = "核心训练";
    private static final String TRAINING_TYPE_REST = "休息恢复";
    private static final String TRAINING_TYPE_MIXED = "综合训练";
    private static final String DEFAULT_DIFFICULTY = "MEDIUM";
    private static final Pattern SETS_REPS_PATTERN =
            Pattern.compile("(\\d+)\\s*(?:组|sets?)\\s*[xX*×]\\s*(\\d+)\\s*(?:次|reps?)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIMPLE_SETS_REPS_PATTERN =
            Pattern.compile("(\\d+)\\s*[xX*×]\\s*(\\d+)");
    private static final Pattern DURATION_PATTERN =
            Pattern.compile("(\\d+)\\s*(?:分钟|min|mins|minutes?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern REST_PATTERN =
            Pattern.compile("(?:休息|rest)\\s*(\\d+)\\s*(?:秒|s|sec|seconds?)?", Pattern.CASE_INSENSITIVE);

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatContextService chatContextService;
    private final AIService aiService;
    private final UserFitnessProfileService userFitnessProfileService;
    private final FitnessPlanMapper fitnessPlanMapper;
    private final FitnessPlanDetailMapper fitnessPlanDetailMapper;
    private final CourseService courseService;
    private final EquipmentService equipmentService;
    private final JianXiaoZhuAgentService jianXiaoZhuAgentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        userMessage.setCreatedAt(LocalDateTime.now());

        chatContextService.saveMessageToDatabase(userMessage);
        chatContextService.addMessage(session.getId(), userMessage);

        String aiResponse = jianXiaoZhuAgentService.chat(userId, session.getId(), dto.getContent());

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(aiResponse);
        assistantMessage.setCreatedAt(LocalDateTime.now());

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
        userMessage.setCreatedAt(LocalDateTime.now());

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
                        ChatMessage assistantMessage = new ChatMessage();
                        assistantMessage.setSessionId(session.getId());
                        assistantMessage.setRole("assistant");
                        assistantMessage.setContent(fullResponse.toString());
                        assistantMessage.setCreatedAt(LocalDateTime.now());

                        chatContextService.saveMessageToDatabase(assistantMessage);
                        chatContextService.addMessage(session.getId(), assistantMessage);
                        updateSessionTitleIfNeeded(session, dto.getContent());

                        log.info("Completed stream message: sessionId={}, userId={}", session.getId(), userId);
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                })
                .doOnError(error -> {
                    SecurityContextHolder.setContext(securityContext);
                    try {
                        log.error("Stream message failed: sessionId={}, userId={}, error={}",
                                session.getId(), userId, error.getMessage());
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public List<ChatSessionVO> getUserSessions(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getIsDeleted, false)
                        .orderByDesc(ChatSession::getUpdatedAt)
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

        session.setIsDeleted(true);
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
                .orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT " + safeLimit);

        if (lastMessageId != null) {
            ChatMessage lastMessage = chatMessageMapper.selectById(lastMessageId);
            if (lastMessage != null) {
                wrapper.lt(ChatMessage::getCreatedAt, lastMessage.getCreatedAt());
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
        planCard.setWeeklyAdvice(generateWeeklyAdvice(goal, experience));
        planCard.setRecommendedCourses(recommendCourses(goal, bodyPart));
        planCard.setRecommendedEquipment(recommendEquipment(bodyPart));

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
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
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
                detail.setCreateTime(LocalDateTime.now());
                detail.setUpdateTime(LocalDateTime.now());
                detail.setDeleted(false);
                detail.setExercisesJson(serializeExercises(dayPlan.getExercises()));
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
        variables.put("availableCourses", buildAvailableCourseCatalog());
        variables.put("availableEquipment", buildAvailableEquipmentCatalog());
        return variables;
    }

    private String buildAvailableCourseCatalog() {
        try {
            List<CourseCardVO> courses = courseService.getHomePageCourseCards(20);
            if (CollUtil.isEmpty(courses)) {
                return "[]";
            }
            return courses.stream()
                    .map(course -> String.format(
                            "- id=%s, name=%s, category=%s, duration=%s",
                            course.getId(),
                            course.getName(),
                            course.getCategory(),
                            course.getDuration()))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("Failed to load course catalog for structured AI plan: {}", e.getMessage());
            return "[]";
        }
    }

    private String buildAvailableEquipmentCatalog() {
        try {
            Page<EquipmentVO> page = equipmentService.getEquipmentList(buildEquipmentQuery(20));
            if (page == null || CollUtil.isEmpty(page.getRecords())) {
                return "[]";
            }
            return page.getRecords().stream()
                    .map(equipment -> String.format(
                            "- id=%s, name=%s, type=%s, location=%s",
                            equipment.getId(),
                            equipment.getEquipmentName(),
                            equipment.getTypeName(),
                            equipment.getLocation()))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("Failed to load equipment catalog for structured AI plan: {}", e.getMessage());
            return "[]";
        }
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
        if (CollUtil.isEmpty(dayPlan.getExercises())) {
            return new ArrayList<>();
        }

        List<ExerciseVO> exercises = new ArrayList<>();
        for (FitnessPlanResponseDTO.ExerciseDTO source : dayPlan.getExercises()) {
            ExerciseVO target = new ExerciseVO();
            target.setName(source.getName());
            target.setSets(source.getSets());
            target.setReps(source.getReps());
            target.setRestTime(source.getRestSeconds());
            target.setTargetMuscle(dayPlan.getFocus());
            target.setDifficulty(DEFAULT_DIFFICULTY);
            exercises.add(target);
        }
        return exercises;
    }

    private List<DayPlanVO> generateLegacyWeeklyPlan(
            UserFitnessProfileVO profile, String goal, String bodyPart, String experience) {
        try {
            String response = aiService.generateFitnessPlan(
                    goal,
                    bodyPart,
                    experience,
                    toInteger(profile.getHeight()),
                    toInteger(profile.getWeight()),
                    profile.getAge()
            );
            if (StrUtil.isBlank(response)) {
                return generateDefaultWeeklyPlan();
            }
            return parseWeeklyPlan(response);
        } catch (Exception e) {
            log.error("Failed to generate legacy fitness plan: userId={}, error={}",
                    profile.getUserId(), e.getMessage());
            throw new BusinessException(ErrorCode.AI_GENERATE_ERROR);
        }
    }

    private List<DayPlanVO> parseWeeklyPlan(String aiResponse) {
        List<DayPlanVO> weeklyPlan = new ArrayList<>();
        DayPlanVO currentDay = null;

        for (String rawLine : aiResponse.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            int dayOfWeek = extractDayOfWeek(line);
            if (dayOfWeek > 0) {
                if (currentDay != null) {
                    finalizeDayPlan(currentDay);
                    weeklyPlan.add(currentDay);
                }
                currentDay = new DayPlanVO();
                currentDay.setDayOfWeek(dayOfWeek);
                currentDay.setDayName(DAY_NAMES[dayOfWeek]);
                currentDay.setTrainingType(extractTrainingType(line));
                currentDay.setFocus(extractFocus(line));
                currentDay.setExercises(new ArrayList<>());
                continue;
            }

            if (currentDay == null) {
                continue;
            }

            if (isTipsLine(line)) {
                currentDay.setDailyTips(stripLinePrefix(line));
                continue;
            }

            ExerciseVO exercise = parseExerciseLine(line);
            if (exercise != null && StrUtil.isNotBlank(exercise.getName())) {
                currentDay.getExercises().add(exercise);
            }
        }

        if (currentDay != null) {
            finalizeDayPlan(currentDay);
            weeklyPlan.add(currentDay);
        }

        return weeklyPlan.isEmpty() ? generateDefaultWeeklyPlan() : weeklyPlan;
    }

    private void finalizeDayPlan(DayPlanVO dayPlan) {
        List<ExerciseVO> exercises = dayPlan.getExercises();
        if (CollUtil.isEmpty(exercises)) {
            exercises = getDefaultExercisesForDay(dayPlan.getDayOfWeek());
            dayPlan.setExercises(exercises);
        }
        if (StrUtil.isBlank(dayPlan.getTrainingType())) {
            dayPlan.setTrainingType(inferTrainingType(dayPlan.getDayOfWeek()));
        }
        if (StrUtil.isBlank(dayPlan.getFocus())) {
            dayPlan.setFocus(defaultFocus(dayPlan.getDayOfWeek()));
        }
        if (dayPlan.getTotalDuration() == null) {
            int duration = exercises.stream()
                    .map(ExerciseVO::getDuration)
                    .filter(value -> value != null && value > 0)
                    .mapToInt(Integer::intValue)
                    .sum();
            dayPlan.setTotalDuration(duration > 0 ? duration : 60);
        }
        if (StrUtil.isBlank(dayPlan.getDailyTips())) {
            dayPlan.setDailyTips("注意保持动作规范，循序渐进。");
        }
    }

    private int extractDayOfWeek(String content) {
        String normalized = content.toLowerCase(Locale.ROOT);
        if (containsAny(content, "周一", "星期一") || normalized.contains("monday") || normalized.contains("day 1")) {
            return 1;
        }
        if (containsAny(content, "周二", "星期二") || normalized.contains("tuesday") || normalized.contains("day 2")) {
            return 2;
        }
        if (containsAny(content, "周三", "星期三") || normalized.contains("wednesday") || normalized.contains("day 3")) {
            return 3;
        }
        if (containsAny(content, "周四", "星期四") || normalized.contains("thursday") || normalized.contains("day 4")) {
            return 4;
        }
        if (containsAny(content, "周五", "星期五") || normalized.contains("friday") || normalized.contains("day 5")) {
            return 5;
        }
        if (containsAny(content, "周六", "星期六") || normalized.contains("saturday") || normalized.contains("day 6")) {
            return 6;
        }
        if (containsAny(content, "周日", "星期日", "星期天") || normalized.contains("sunday") || normalized.contains("day 7")) {
            return 7;
        }
        return 0;
    }

    private String extractTrainingType(String content) {
        String normalized = content.toLowerCase(Locale.ROOT);
        if (containsAny(content, "休息", "恢复") || normalized.contains("rest") || normalized.contains("recovery")) {
            return TRAINING_TYPE_REST;
        }
        if (containsAny(content, "有氧", "跑步", "骑行") || normalized.contains("cardio")) {
            return TRAINING_TYPE_CARDIO;
        }
        if (containsAny(content, "核心") || normalized.contains("core")) {
            return TRAINING_TYPE_CORE;
        }
        if (containsAny(content, "力量", "器械", "胸", "背", "肩", "腿") || normalized.contains("strength")) {
            return TRAINING_TYPE_STRENGTH;
        }
        return TRAINING_TYPE_MIXED;
    }

    private String extractFocus(String content) {
        if (containsAny(content, "胸")) {
            return "胸部";
        }
        if (containsAny(content, "背")) {
            return "背部";
        }
        if (containsAny(content, "肩")) {
            return "肩部";
        }
        if (containsAny(content, "腿")) {
            return "腿部";
        }
        if (containsAny(content, "核心", "腹")) {
            return "核心";
        }
        if (containsAny(content, "休息", "恢复")) {
            return "恢复";
        }
        return null;
    }

    private ExerciseVO parseExerciseLine(String line) {
        String normalizedLine = stripLinePrefix(line);
        if (normalizedLine.isEmpty()) {
            return null;
        }

        ExerciseVO exercise = new ExerciseVO();
        exercise.setName(extractExerciseName(normalizedLine));
        exercise.setRestTime(60);
        exercise.setDifficulty(DEFAULT_DIFFICULTY);

        Matcher setsRepsMatcher = SETS_REPS_PATTERN.matcher(normalizedLine);
        if (setsRepsMatcher.find()) {
            exercise.setSets(Integer.parseInt(setsRepsMatcher.group(1)));
            exercise.setReps(Integer.parseInt(setsRepsMatcher.group(2)));
        } else {
            Matcher simpleMatcher = SIMPLE_SETS_REPS_PATTERN.matcher(normalizedLine);
            if (simpleMatcher.find()) {
                exercise.setSets(Integer.parseInt(simpleMatcher.group(1)));
                exercise.setReps(Integer.parseInt(simpleMatcher.group(2)));
            }
        }

        Matcher durationMatcher = DURATION_PATTERN.matcher(normalizedLine);
        if (durationMatcher.find()) {
            exercise.setDuration(Integer.parseInt(durationMatcher.group(1)));
        }

        Matcher restMatcher = REST_PATTERN.matcher(normalizedLine);
        if (restMatcher.find()) {
            exercise.setRestTime(Integer.parseInt(restMatcher.group(1)));
        }

        int leftBracket = normalizedLine.indexOf('(');
        int rightBracket = normalizedLine.indexOf(')');
        if (leftBracket >= 0 && rightBracket > leftBracket) {
            exercise.setTips(normalizedLine.substring(leftBracket + 1, rightBracket).trim());
        }

        return exercise;
    }

    private List<DayPlanVO> generateDefaultWeeklyPlan() {
        List<DayPlanVO> weeklyPlan = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            DayPlanVO dayPlan = new DayPlanVO();
            dayPlan.setDayOfWeek(i);
            dayPlan.setDayName(DAY_NAMES[i]);
            dayPlan.setTrainingType(inferTrainingType(i));
            dayPlan.setFocus(defaultFocus(i));
            dayPlan.setExercises(getDefaultExercisesForDay(i));
            dayPlan.setTotalDuration(60);
            dayPlan.setDailyTips("注意保持动作规范，循序渐进。");
            weeklyPlan.add(dayPlan);
        }
        return weeklyPlan;
    }

    private List<ExerciseVO> getDefaultExercisesForDay(int dayOfWeek) {
        List<ExerciseVO> exercises = new ArrayList<>();
        switch (dayOfWeek) {
            case 1:
                exercises.add(createExercise("俯卧撑", 3, 15, null, "胸部"));
                exercises.add(createExercise("哑铃卧推", 4, 12, null, "胸部"));
                exercises.add(createExercise("飞鸟", 3, 15, null, "胸部"));
                break;
            case 2:
                exercises.add(createExercise("引体向上", 3, 8, null, "背部"));
                exercises.add(createExercise("哑铃划船", 4, 12, null, "背部"));
                exercises.add(createExercise("硬拉", 3, 10, null, "背部"));
                break;
            case 3:
                exercises.add(createExercise("跑步", null, null, 30, "全身"));
                exercises.add(createExercise("跳绳", null, null, 15, "全身"));
                break;
            case 4:
                exercises.add(createExercise("哑铃推举", 4, 12, null, "肩部"));
                exercises.add(createExercise("侧平举", 3, 15, null, "肩部"));
                exercises.add(createExercise("前平举", 3, 12, null, "肩部"));
                break;
            case 5:
                exercises.add(createExercise("深蹲", 4, 15, null, "腿部"));
                exercises.add(createExercise("弓步蹲", 3, 12, null, "腿部"));
                exercises.add(createExercise("提踵", 4, 20, null, "腿部"));
                break;
            case 6:
                exercises.add(createExercise("平板支撑", 3, null, 1, "核心"));
                exercises.add(createExercise("卷腹", 4, 20, null, "核心"));
                exercises.add(createExercise("俄罗斯转体", 3, 30, null, "核心"));
                break;
            case 7:
                exercises.add(createExercise("瑜伽拉伸", null, null, 30, "全身"));
                exercises.add(createExercise("散步", null, null, 20, "全身"));
                break;
            default:
                break;
        }
        return exercises;
    }

    private ExerciseVO createExercise(String name, Integer sets, Integer reps, Integer duration, String targetMuscle) {
        ExerciseVO exercise = new ExerciseVO();
        exercise.setName(name);
        exercise.setSets(sets);
        exercise.setReps(reps);
        exercise.setDuration(duration);
        exercise.setTargetMuscle(targetMuscle);
        exercise.setRestTime(60);
        exercise.setDifficulty(DEFAULT_DIFFICULTY);
        return exercise;
    }

    private WeeklyAdviceVO generateWeeklyAdvice(String goal, String experience) {
        WeeklyAdviceVO advice = new WeeklyAdviceVO();
        advice.setOverallGoal("根据你的训练目标制定 7 天循序渐进计划。");
        advice.setTrainingPrinciples("先保证动作标准，再逐步增加训练量。");
        advice.setDietAdvice("保证蛋白质摄入，控制总热量，注意补水。");
        advice.setRestAdvice("保持规律睡眠，训练后进行轻度拉伸恢复。");
        advice.setSafetyTips("训练前热身，训练中如有疼痛或不适应立即停止。");
        advice.setProgressionTips("每周根据恢复情况微调重量、次数或训练时长。");
        return advice;
    }

    private List<CourseRecommendVO> recommendCourses(String goal, String bodyPart) {
        List<CourseRecommendVO> courses = new ArrayList<>();
        try {
            List<CourseCardVO> courseCards = courseService.getHomePageCourseCards(6);
            for (CourseCardVO card : courseCards) {
                CourseRecommendVO recommend = new CourseRecommendVO();
                recommend.setCourseId(card.getId());
                recommend.setCourseName(card.getName());
                recommend.setCategory(card.getCategory());
                recommend.setImageUrl(card.getImage());
                recommend.setDifficultyLevel(card.getLevel());
                recommend.setDurationMinutes(card.getDuration());
                recommend.setRecommendReason("适合当前 " + goal + " / " + bodyPart + " 训练目标。");
                courses.add(recommend);
            }
        } catch (Exception e) {
            log.warn("Failed to load recommended courses: {}", e.getMessage());
        }
        return courses;
    }

    private List<EquipmentRecommendVO> recommendEquipment(String bodyPart) {
        List<EquipmentRecommendVO> equipments = new ArrayList<>();
        try {
            Page<EquipmentVO> page = equipmentService.getEquipmentList(buildEquipmentQuery(10));
            if (page != null && CollUtil.isNotEmpty(page.getRecords())) {
                for (EquipmentVO equipment : page.getRecords()) {
                    EquipmentRecommendVO recommend = new EquipmentRecommendVO();
                    recommend.setEquipmentId(equipment.getId());
                    recommend.setEquipmentName(equipment.getEquipmentName());
                    recommend.setTypeName(equipment.getTypeName());
                    recommend.setLocation(equipment.getLocation());
                    recommend.setImageUrl(equipment.getImageUrl());
                    recommend.setDescription(equipment.getDescription());
                    recommend.setRecommendReason("适合 " + bodyPart + " 训练使用。");
                    equipments.add(recommend);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load recommended equipment: {}", e.getMessage());
        }
        return equipments;
    }

    private List<DayPlanVO> convertDetailsToWeeklyPlan(List<FitnessPlanDetail> details) {
        Map<Integer, List<FitnessPlanDetail>> groupedByDay = details.stream()
                .collect(Collectors.groupingBy(FitnessPlanDetail::getDayIndex));

        List<DayPlanVO> weeklyPlan = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            DayPlanVO dayPlan = new DayPlanVO();
            dayPlan.setDayOfWeek(i);
            dayPlan.setDayName(DAY_NAMES[i]);

            List<FitnessPlanDetail> dayDetails = groupedByDay.getOrDefault(i, new ArrayList<>());
            if (!dayDetails.isEmpty()) {
                FitnessPlanDetail firstDetail = dayDetails.get(0);
                dayPlan.setDayName(StrUtil.blankToDefault(firstDetail.getDayName(), DAY_NAMES[i]));
                dayPlan.setFocus(firstDetail.getFocus());
                dayPlan.setTotalDuration(firstDetail.getDuration());
                dayPlan.setDailyTips(firstDetail.getNotes());
            }

            List<ExerciseVO> exercises = new ArrayList<>();
            for (FitnessPlanDetail detail : dayDetails) {
                if (StrUtil.isNotBlank(detail.getExercisesJson())) {
                    exercises.addAll(parseExercises(detail.getExercisesJson()));
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

    private List<ExerciseVO> parseExercises(String exercisesJson) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawExercises = objectMapper.readValue(exercisesJson, List.class);
            List<ExerciseVO> exercises = new ArrayList<>();
            for (Map<String, Object> raw : rawExercises) {
                ExerciseVO exercise = new ExerciseVO();
                exercise.setName((String) raw.get("name"));
                exercise.setSets(asInteger(raw.get("sets")));
                exercise.setReps(asInteger(raw.get("reps")));
                exercise.setDuration(asInteger(raw.get("duration")));
                exercise.setTips((String) raw.get("tips"));
                exercise.setRestTime(asInteger(raw.get("restTime")));
                exercise.setTargetMuscle((String) raw.get("targetMuscle"));
                exercise.setDifficulty((String) raw.getOrDefault("difficulty", DEFAULT_DIFFICULTY));
                exercises.add(exercise);
            }
            return exercises;
        } catch (Exception e) {
            log.warn("Failed to parse exercises JSON: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String serializeExercises(List<ExerciseVO> exercises) {
        if (CollUtil.isEmpty(exercises)) {
            return null;
        }

        try {
            List<Map<String, Object>> rawExercises = new ArrayList<>();
            for (ExerciseVO exercise : exercises) {
                Map<String, Object> raw = new HashMap<>();
                raw.put("name", exercise.getName());
                raw.put("sets", exercise.getSets());
                raw.put("reps", exercise.getReps());
                raw.put("duration", exercise.getDuration());
                raw.put("restTime", exercise.getRestTime());
                raw.put("targetMuscle", exercise.getTargetMuscle());
                raw.put("tips", exercise.getTips());
                raw.put("difficulty", exercise.getDifficulty());
                rawExercises.add(raw);
            }
            return objectMapper.writeValueAsString(rawExercises);
        } catch (Exception e) {
            log.warn("Failed to serialize exercises JSON: {}", e.getMessage());
            return null;
        }
    }

    private ChatSession getOrCreateSession(Long userId, Long sessionId) {
        if (sessionId == null) {
            return createSessionEntity(userId);
        }

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
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setIsDeleted(false);
        chatSessionMapper.insert(session);
        return session;
    }

    private void updateSessionTitleIfNeeded(ChatSession session, String firstMessage) {
        if (!DEFAULT_SESSION_TITLE.equals(session.getTitle())) {
            return;
        }
        session.setTitle(generateSessionTitle(firstMessage));
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.updateById(session);
    }

    private String generateSessionTitle(String firstMessage) {
        if (StrUtil.isBlank(firstMessage)) {
            return DEFAULT_SESSION_TITLE;
        }
        return firstMessage.length() <= 20 ? firstMessage : firstMessage.substring(0, 20) + "...";
    }

    private ChatSessionVO convertToSessionVO(ChatSession session) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setCreatedAt(session.getCreatedAt());
        vo.setUpdatedAt(session.getUpdatedAt());
        return vo;
    }

    private ChatMessageVO convertToMessageVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }

    private String generatePlanName(String goal, String bodyPart) {
        return goal + "-" + bodyPart + "训练计划";
    }

    private EquipmentQueryDTO buildEquipmentQuery(int pageSize) {
        EquipmentQueryDTO query = new EquipmentQueryDTO();
        query.setStatus(1);
        query.setPageNum(1);
        query.setPageSize(pageSize);
        return query;
    }

    private boolean isTipsLine(String line) {
        return containsAny(line, "注意", "建议", "提示", "tips", "note");
    }

    private String stripLinePrefix(String line) {
        return line.replaceFirst("^[\\-•*\\d.、\\s]+", "").trim();
    }

    private String extractExerciseName(String line) {
        String candidate = line.split("[（(:：-]")[0].trim();
        return StrUtil.blankToDefault(candidate, line);
    }

    private String inferTrainingType(int dayOfWeek) {
        switch (dayOfWeek) {
            case 3:
                return TRAINING_TYPE_CARDIO;
            case 6:
                return TRAINING_TYPE_CORE;
            case 7:
                return TRAINING_TYPE_REST;
            default:
                return TRAINING_TYPE_STRENGTH;
        }
    }

    private String defaultFocus(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return "胸部";
            case 2:
                return "背部";
            case 3:
                return "全身";
            case 4:
                return "肩部";
            case 5:
                return "腿部";
            case 6:
                return "核心";
            case 7:
                return "恢复";
            default:
                return "综合";
        }
    }

    private boolean containsAny(String source, String... values) {
        for (String value : values) {
            if (source.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private Integer toInteger(Number value) {
        return value == null ? null : value.intValue();
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String && StrUtil.isNotBlank((String) value)) {
            return Integer.parseInt((String) value);
        }
        return null;
    }
}
