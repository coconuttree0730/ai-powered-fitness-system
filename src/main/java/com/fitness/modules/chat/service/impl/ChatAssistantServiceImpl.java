package com.fitness.modules.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.chat.agent.JianXiaoZhuAgentService;
import com.fitness.modules.chat.mapper.ChatMessageMapper;
import com.fitness.modules.chat.mapper.ChatSessionMapper;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.entity.ChatMessage;
import com.fitness.modules.chat.model.entity.ChatSession;
import com.fitness.modules.chat.model.vo.*;
import com.fitness.modules.chat.service.ChatAssistantService;
import com.fitness.modules.chat.service.ChatContextService;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.service.CourseService;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.service.EquipmentService;
import com.fitness.modules.plan.model.entity.FitnessPlan;
import com.fitness.modules.plan.model.entity.FitnessPlanDetail;
import com.fitness.modules.plan.mapper.FitnessPlanMapper;
import com.fitness.modules.plan.mapper.FitnessPlanDetailMapper;
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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAssistantServiceImpl implements ChatAssistantService {

    private static final String DEFAULT_SESSION_TITLE = "\u65b0\u5bf9\u8bdd";

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

    // 最大上下文消息数（消息框可显示的消息数）
    //private static final int MAX_CONTEXT_MESSAGES = 10;

    @Override
    @Transactional
    public ChatSessionVO createSession(Long userId) {

        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(DEFAULT_SESSION_TITLE);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setIsDeleted(false);

        chatSessionMapper.insert(session);

        log.info("创建新会话: sessionId={}, userId={}", session.getId(), userId);

        return convertToSessionVO(session);
    }

    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long userId, ChatMessageDTO dto) {
        ChatSession session;
        if (dto.getSessionId() == null) {
            session = createSessionEntity(userId);
        } else {
            session = chatSessionMapper.selectById(dto.getSessionId());
            if (session == null || !session.getUserId().equals(userId)) {
                throw new BusinessException("会话不存在或无权访问");
            }
        }

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

        if (DEFAULT_SESSION_TITLE.equals(session.getTitle())) {
            session.setTitle(generateSessionTitle(dto.getContent()));
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionMapper.updateById(session);
        }

        log.info("发送消息: sessionId={}, userId={}, 消息长度={}", session.getId(), userId, dto.getContent().length());

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
                return Flux.error(new BusinessException("\u4f1a\u8bdd\u4e0d\u5b58\u5728\u6216\u65e0\u6743\u8bbf\u95ee"));
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

                        if (DEFAULT_SESSION_TITLE.equals(session.getTitle())) {
                            session.setTitle(generateSessionTitle(dto.getContent()));
                            session.setUpdatedAt(LocalDateTime.now());
                            chatSessionMapper.updateById(session);
                        }

                        log.info("Stream message completed: sessionId={}, userId={}", session.getId(), userId);
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

        ChatSessionVO vo = convertToSessionVO(session);

        return vo;
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

        log.info("删除会话: sessionId={}, userId={}", sessionId, userId);
    }

    @Override
    public List<ChatMessageVO> getSessionMessages(Long sessionId, Long userId, Long lastMessageId, Integer limit) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在或无权访问");
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
            .eq(ChatMessage::getSessionId, sessionId)
            .orderByDesc(ChatMessage::getCreatedAt)
            .last("LIMIT " + limit);

        if (lastMessageId != null) {
            ChatMessage lastMessage = chatMessageMapper.selectById(lastMessageId);
            if (lastMessage != null) {
                wrapper.lt(ChatMessage::getCreatedAt, lastMessage.getCreatedAt());
            }
        }

        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);

        return messages.stream()
            .map(this::convertToMessageVO)
            .collect(Collectors.toList());
    }

    @Override
    public FitnessPlanCardVO generateFitnessPlanCard(Long userId, String goal, String bodyPart, String experience) {
        // 检查用户是否已完善个人信息
        if (!userFitnessProfileService.isProfileComplete(userId)) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETE);
        }

        // 获取用户健身档案
        UserFitnessProfileVO profile = userFitnessProfileService.getProfile(userId);

        // 调用AI服务生成计划
        String aiResponse;
        try {
            aiResponse = aiService.generateFitnessPlan(
                    goal,
                    bodyPart,
                    experience,
                    profile.getHeight() != null ? profile.getHeight().intValue() : null,
                    profile.getWeight() != null ? profile.getWeight().intValue() : null,
                    profile.getAge()
            );
        } catch (Exception e) {
            log.error("AI生成计划失败: userId={}, error={}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.AI_GENERATE_ERROR);
        }

        // 构建健身计划卡片
        FitnessPlanCardVO planCard = new FitnessPlanCardVO();
        planCard.setPlanName(generatePlanName(goal, bodyPart));
        planCard.setGoal(goal);
        planCard.setBodyPart(bodyPart);
        planCard.setExperience(experience);
        planCard.setDuration(7);

        // 解析AI响应生成每周计划
        List<DayPlanVO> weeklyPlan = parseWeeklyPlan(aiResponse);
        planCard.setWeeklyPlan(weeklyPlan);

        // 生成每周建议
        planCard.setWeeklyAdvice(generateWeeklyAdvice(goal, experience));

        // 推荐相关课程
        planCard.setRecommendedCourses(recommendCourses(goal, bodyPart));

        // 推荐相关器械
        planCard.setRecommendedEquipment(recommendEquipment(bodyPart));

        log.info("健身计划卡片生成成功: userId={}, goal={}, bodyPart={}", userId, goal, bodyPart);

        return planCard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveFitnessPlan(Long userId, FitnessPlanCardVO planCard) {
        // 创建计划主表记录
        FitnessPlan plan = new FitnessPlan();
        plan.setUserId(userId);
        plan.setPlanName(planCard.getPlanName());
        plan.setGoal(planCard.getGoal());
        plan.setDuration(planCard.getDuration());
        plan.setStatus(1); // 进行中
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        plan.setDeleted(false);

        fitnessPlanMapper.insert(plan);

        // 保存计划详情
        if (CollUtil.isNotEmpty(planCard.getWeeklyPlan())) {
            int sortOrder = 0;
            for (DayPlanVO dayPlan : planCard.getWeeklyPlan()) {
                FitnessPlanDetail detail = new FitnessPlanDetail();
                detail.setPlanId(plan.getId());
                detail.setDayIndex(dayPlan.getDayOfWeek());
                detail.setDayName(dayPlan.getDayName());
                detail.setFocus(dayPlan.getFocus());
                detail.setSortOrder(sortOrder++);
                detail.setCreateTime(LocalDateTime.now());
                detail.setUpdateTime(LocalDateTime.now());
                detail.setDeleted(false);

                // 将训练动作转换为 JSON 存储
                if (CollUtil.isNotEmpty(dayPlan.getExercises())) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        List<Map<String, Object>> exercisesJson = new ArrayList<>();
                        for (ExerciseVO exercise : dayPlan.getExercises()) {
                            Map<String, Object> ex = new HashMap<>();
                            ex.put("name", exercise.getName());
                            ex.put("sets", exercise.getSets());
                            ex.put("reps", exercise.getReps());
                            ex.put("duration", exercise.getDuration());
                            ex.put("tips", exercise.getTips());
                            exercisesJson.add(ex);
                        }
                        detail.setExercisesJson(objectMapper.writeValueAsString(exercisesJson));
                    } catch (Exception e) {
                        log.warn("序列化训练动作失败: {}", e.getMessage());
                    }
                }

                fitnessPlanDetailMapper.insert(detail);
            }
        }

        log.info("健身计划保存成功: userId={}, planId={}", userId, plan.getId());
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

            // 查询计划详情
            List<FitnessPlanDetail> details = fitnessPlanDetailMapper.selectByPlanId(plan.getId());
            List<DayPlanVO> weeklyPlan = convertDetailsToWeeklyPlan(details);
            vo.setWeeklyPlan(weeklyPlan);

            result.add(vo);
        }

        return result;
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

    private String generateSessionTitle(String firstMessage) {
        if (StrUtil.isBlank(firstMessage)) {
            return DEFAULT_SESSION_TITLE;
        }

        int maxLength = 20;
        if (firstMessage.length() <= maxLength) {
            return firstMessage;
        }
        return firstMessage.substring(0, maxLength) + "...";
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

    /**
     * 生成计划名称
     */
    private String generatePlanName(String goal, String bodyPart) {
        return goal + "-" + bodyPart + "训练计划";
    }

    /**
     * 解析AI响应生成每周计划
     */
    private List<DayPlanVO> parseWeeklyPlan(String aiResponse) {
        List<DayPlanVO> weeklyPlan = new ArrayList<>();
        String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        // 按天分割内容
        String[] days = aiResponse.split("(?=第[一二三四五六七日1234567]天|周[一二三四五六日1234567])");

        for (String dayContent : days) {
            if (dayContent.trim().isEmpty()) {
                continue;
            }

            // 提取星期几
            int dayOfWeek = extractDayOfWeek(dayContent);
            if (dayOfWeek == 0) {
                continue;
            }

            DayPlanVO dayPlan = new DayPlanVO();
            dayPlan.setDayOfWeek(dayOfWeek);
            dayPlan.setDayName(dayNames[dayOfWeek]);

            // 提取训练类型
            dayPlan.setTrainingType(extractTrainingType(dayContent));

            // 提取训练动作
            List<ExerciseVO> exercises = extractExercises(dayContent, dayOfWeek);
            dayPlan.setExercises(exercises);

            // 计算总时长
            int totalDuration = exercises.stream()
                    .mapToInt(e -> e.getDuration() != null ? e.getDuration() : 0)
                    .sum();
            dayPlan.setTotalDuration(totalDuration > 0 ? totalDuration : 60);

            // 提取每日建议
            dayPlan.setDailyTips(extractDailyTips(dayContent));

            weeklyPlan.add(dayPlan);
        }

        // 如果没有解析出任何内容，使用默认计划
        if (weeklyPlan.isEmpty()) {
            weeklyPlan = generateDefaultWeeklyPlan();
        }

        return weeklyPlan;
    }

    /**
     * 提取星期几
     */
    private int extractDayOfWeek(String content) {
        if (content.contains("第一天") || content.contains("第1天") || content.contains("周一") || content.contains("周1")) return 1;
        if (content.contains("第二天") || content.contains("第2天") || content.contains("周二") || content.contains("周2")) return 2;
        if (content.contains("第三天") || content.contains("第3天") || content.contains("周三") || content.contains("周3")) return 3;
        if (content.contains("第四天") || content.contains("第4天") || content.contains("周四") || content.contains("周4")) return 4;
        if (content.contains("第五天") || content.contains("第5天") || content.contains("周五") || content.contains("周5")) return 5;
        if (content.contains("第六天") || content.contains("第6天") || content.contains("周六") || content.contains("周6")) return 6;
        if (content.contains("第七天") || content.contains("第7天") || content.contains("周日") || content.contains("周7") || content.contains("第七天") || content.contains("第日天"))
            return 7;
        return 0;
    }

    /**
     * 提取训练类型
     */
    private String extractTrainingType(String content) {
        if (content.contains("休息") || content.contains("恢复")) return "休息恢复";
        if (content.contains("有氧") || content.contains("跑步") || content.contains("游泳")) return "有氧训练";
        if (content.contains("力量") || content.contains("器械")) return "力量训练";
        if (content.contains("核心")) return "核心训练";
        return "综合训练";
    }

    /**
     * 提取训练动作
     */
    private List<ExerciseVO> extractExercises(String content, int dayOfWeek) {
        List<ExerciseVO> exercises = new ArrayList<>();

        String[] lines = content.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.contains("第") && line.contains("天")) {
                continue;
            }

            // 跳过非训练内容的行
            if (line.contains("注意事项") || line.contains("建议") || line.contains("总时长")) {
                continue;
            }

            ExerciseVO exercise = parseExerciseLine(line);
            if (exercise != null && StrUtil.isNotBlank(exercise.getName())) {
                exercises.add(exercise);
            }
        }

        // 如果没有解析出动作，添加默认动作
        if (exercises.isEmpty()) {
            exercises = getDefaultExercisesForDay(dayOfWeek);
        }

        return exercises;
    }

    /**
     * 解析单行训练内容
     */
    private ExerciseVO parseExerciseLine(String line) {
        ExerciseVO exercise = new ExerciseVO();

        // 提取动作名称（通常是行首的内容）
        String name = line.split("[：:]")[0].trim();
        exercise.setName(name);

        // 尝试提取组数和次数
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)[组]\\s*[xX*]\\s*(\\d+)[次个]");
        java.util.regex.Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            exercise.setSets(Integer.parseInt(matcher.group(1)));
            exercise.setReps(Integer.parseInt(matcher.group(2)));
        }

        // 尝试提取持续时间
        java.util.regex.Pattern durationPattern = java.util.regex.Pattern.compile("(\\d+)[分钟min]");
        java.util.regex.Matcher durationMatcher = durationPattern.matcher(line);
        if (durationMatcher.find()) {
            exercise.setDuration(Integer.parseInt(durationMatcher.group(1)));
        }

        // 提取休息时间
        java.util.regex.Pattern restPattern = java.util.regex.Pattern.compile("休息(\\d+)[秒s]");
        java.util.regex.Matcher restMatcher = restPattern.matcher(line);
        if (restMatcher.find()) {
            exercise.setRestTime(Integer.parseInt(restMatcher.group(1)));
        } else {
            exercise.setRestTime(60); // 默认60秒
        }

        // 提取要点/注意事项
        if (line.contains("(")) {
            int start = line.indexOf("(");
            int end = line.indexOf(")");
            if (end > start) {
                exercise.setTips(line.substring(start + 1, end));
            }
        }

        // 设置难度
        exercise.setDifficulty("MEDIUM");

        return exercise;
    }

    /**
     * 提取每日建议
     */
    private String extractDailyTips(String content) {
        if (content.contains("注意事项")) {
            int start = content.indexOf("注意事项");
            int end = content.indexOf("\\n", start);
            if (end > start) {
                return content.substring(start, end).trim();
            }
        }
        return "注意保持正确姿势，量力而行";
    }

    /**
     * 生成默认每周计划
     */
    private List<DayPlanVO> generateDefaultWeeklyPlan() {
        List<DayPlanVO> weeklyPlan = new ArrayList<>();
        String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        for (int i = 1; i <= 7; i++) {
            DayPlanVO dayPlan = new DayPlanVO();
            dayPlan.setDayOfWeek(i);
            dayPlan.setDayName(dayNames[i]);
            dayPlan.setExercises(getDefaultExercisesForDay(i));
            dayPlan.setTotalDuration(60);
            weeklyPlan.add(dayPlan);
        }

        return weeklyPlan;
    }

    /**
     * 获取每天的默认训练动作
     */
    private List<ExerciseVO> getDefaultExercisesForDay(int dayOfWeek) {
        List<ExerciseVO> exercises = new ArrayList<>();

        switch (dayOfWeek) {
            case 1: // 周一：胸部
                exercises.add(createExercise("俯卧撑", 3, 15, null, "胸部"));
                exercises.add(createExercise("哑铃卧推", 4, 12, null, "胸部"));
                exercises.add(createExercise("飞鸟", 3, 15, null, "胸部"));
                break;
            case 2: // 周二：背部
                exercises.add(createExercise("引体向上", 3, 8, null, "背部"));
                exercises.add(createExercise("哑铃划船", 4, 12, null, "背部"));
                exercises.add(createExercise("硬拉", 3, 10, null, "背部"));
                break;
            case 3: // 周三：有氧
                exercises.add(createExercise("跑步", null, null, 30, "全身"));
                exercises.add(createExercise("跳绳", null, null, 15, "全身"));
                break;
            case 4: // 周四：肩部
                exercises.add(createExercise("哑铃推举", 4, 12, null, "肩部"));
                exercises.add(createExercise("侧平举", 3, 15, null, "肩部"));
                exercises.add(createExercise("前平举", 3, 12, null, "肩部"));
                break;
            case 5: // 周五：腿部
                exercises.add(createExercise("深蹲", 4, 15, null, "腿部"));
                exercises.add(createExercise("弓步蹲", 3, 12, null, "腿部"));
                exercises.add(createExercise("小腿提踵", 4, 20, null, "腿部"));
                break;
            case 6: // 周六：核心
                exercises.add(createExercise("平板支撑", 3, null, 1, "核心"));
                exercises.add(createExercise("卷腹", 4, 20, null, "核心"));
                exercises.add(createExercise("俄罗斯转体", 3, 30, null, "核心"));
                break;
            case 7: // 周日：休息
                exercises.add(createExercise("瑜伽拉伸", null, null, 30, "全身"));
                exercises.add(createExercise("散步", null, null, 20, "全身"));
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
        exercise.setDifficulty("MEDIUM");
        return exercise;
    }

    /**
     * 生成每周建议
     */
    private WeeklyAdviceVO generateWeeklyAdvice(String goal, String experience) {
        WeeklyAdviceVO advice = new WeeklyAdviceVO();

        advice.setOverallGoal("根据您的" + goal + "目标，制定科学的训练计划");
        advice.setTrainingPrinciples("循序渐进，量力而行，保持规律");
        advice.setDietAdvice("注意蛋白质摄入，保持水分补充，控制碳水化合物");
        advice.setRestAdvice("保证每天7-8小时睡眠，训练后适当拉伸放松");
        advice.setSafetyTips("训练前充分热身，注意动作规范，感到不适立即停止");
        advice.setProgressionTips("每周适当增加重量或次数，记录训练数据追踪进步");

        return advice;
    }

    /**
     * 推荐相关课程
     */
    private List<CourseRecommendVO> recommendCourses(String goal, String bodyPart) {
        List<CourseRecommendVO> courses = new ArrayList<>();

        try {
            // 获取首页课程卡片
            List<CourseCardVO> courseCards = courseService.getHomePageCourseCards(6);

            for (CourseCardVO card : courseCards) {
                CourseRecommendVO recommend = new CourseRecommendVO();
                recommend.setCourseId(card.getId());
                recommend.setCourseName(card.getName());
                recommend.setCategory(card.getCategory());
                recommend.setImageUrl(card.getImage());
                recommend.setDifficultyLevel(card.getLevel());
                recommend.setDurationMinutes(card.getDuration());
                recommend.setRecommendReason("根据您的" + goal + "目标推荐");
                courses.add(recommend);
            }
        } catch (Exception e) {
            log.warn("获取推荐课程失败: {}", e.getMessage());
        }

        return courses;
    }

    /**
     * 推荐相关器械
     */
    private List<EquipmentRecommendVO> recommendEquipment(String bodyPart) {
        List<EquipmentRecommendVO> equipments = new ArrayList<>();

        try {
            // 获取所有正常状态的器械
            com.fitness.modules.equipment.model.dto.EquipmentQueryDTO query =
                    new com.fitness.modules.equipment.model.dto.EquipmentQueryDTO();
            query.setStatus(1);
            query.setPageNum(1);
            query.setPageSize(10);

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<EquipmentVO> page =
                    equipmentService.getEquipmentList(query);

            if (page != null && CollUtil.isNotEmpty(page.getRecords())) {
                for (EquipmentVO equipment : page.getRecords()) {
                    EquipmentRecommendVO recommend = new EquipmentRecommendVO();
                    recommend.setEquipmentId(equipment.getId());
                    recommend.setEquipmentName(equipment.getEquipmentName());
                    recommend.setTypeName(equipment.getTypeName());
                    recommend.setLocation(equipment.getLocation());
                    recommend.setImageUrl(equipment.getImageUrl());
                    recommend.setDescription(equipment.getDescription());
                    recommend.setRecommendReason("适合" + bodyPart + "训练使用");
                    equipments.add(recommend);
                }
            }
        } catch (Exception e) {
            log.warn("获取推荐器械失败: {}", e.getMessage());
        }

        return equipments;
    }

    /**
     * 将计划详情转换为每周计划
     */
    private List<DayPlanVO> convertDetailsToWeeklyPlan(List<FitnessPlanDetail> details) {
        Map<Integer, List<FitnessPlanDetail>> groupedByDay = details.stream()
                .collect(Collectors.groupingBy(FitnessPlanDetail::getDayIndex));

        List<DayPlanVO> weeklyPlan = new ArrayList<>();
        String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        for (int i = 1; i <= 7; i++) {
            DayPlanVO dayPlan = new DayPlanVO();
            dayPlan.setDayOfWeek(i);
            dayPlan.setDayName(dayNames[i]);

            List<FitnessPlanDetail> dayDetails = groupedByDay.getOrDefault(i, new ArrayList<>());

            // 获取第一条记录的信息
            if (!dayDetails.isEmpty()) {
                FitnessPlanDetail firstDetail = dayDetails.get(0);
                dayPlan.setFocus(firstDetail.getFocus());
            }

            // 解析训练动作 JSON
            List<ExerciseVO> exercises = new ArrayList<>();
            for (FitnessPlanDetail detail : dayDetails) {
                if (StrUtil.isNotBlank(detail.getExercisesJson())) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> exercisesJson = objectMapper.readValue(detail.getExercisesJson(), List.class);
                        for (Map<String, Object> ex : exercisesJson) {
                            ExerciseVO exercise = new ExerciseVO();
                            exercise.setName((String) ex.get("name"));
                            exercise.setSets((Integer) ex.get("sets"));
                            exercise.setReps((Integer) ex.get("reps"));
                            exercise.setDuration((Integer) ex.get("duration"));
                            exercise.setTips((String) ex.get("tips"));
                            exercises.add(exercise);
                        }
                    } catch (Exception e) {
                        log.warn("解析训练动作JSON失败: {}", e.getMessage());
                    }
                }
            }

            dayPlan.setExercises(exercises);
            weeklyPlan.add(dayPlan);
        }

        return weeklyPlan;
    }
}
