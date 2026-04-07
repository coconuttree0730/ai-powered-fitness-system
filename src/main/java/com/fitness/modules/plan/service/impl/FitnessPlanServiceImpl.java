package com.fitness.modules.plan.service.impl;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.service.CourseService;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.service.EquipmentService;
import com.fitness.modules.plan.mapper.FitnessPlanDetailMapper;
import com.fitness.modules.plan.mapper.FitnessPlanMapper;
import com.fitness.modules.plan.model.dto.PlanGenerateDTO;
import com.fitness.modules.plan.model.entity.FitnessPlan;
import com.fitness.modules.plan.model.entity.FitnessPlanDetail;
import com.fitness.modules.plan.model.vo.PlanDetailVO;
import com.fitness.modules.plan.model.vo.PlanVO;
import com.fitness.modules.plan.service.FitnessPlanService;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;
import com.fitness.modules.user.service.UserFitnessProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 健身计划服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FitnessPlanServiceImpl implements FitnessPlanService {

    private final FitnessPlanMapper fitnessPlanMapper;
    private final FitnessPlanDetailMapper fitnessPlanDetailMapper;
    private final AIService aiService;
    private final UserFitnessProfileService userFitnessProfileService;
    private final CourseService courseService;
    private final EquipmentService equipmentService;
    private final ObjectMapper objectMapper;

    // 缓存课程和器械数据，用于校验LLM返回的数据
    private List<CourseCardVO> cachedCourses;
    private List<EquipmentVO> cachedEquipment;

    @Override
    public Map<String, Object> generatePlanFromProfile(Long userId) {
        log.info("从个人档案生成健身计划: userId={}", userId);

        // 1. 获取用户健身档案
        if (!userFitnessProfileService.isProfileComplete(userId)) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETE);
        }
        UserFitnessProfileVO profile = userFitnessProfileService.getProfile(userId);

        // 2. 计算BMI
        BigDecimal height = profile.getHeight();
        BigDecimal weight = profile.getWeight();
        String bmi = "22.9";
        if (height != null && weight != null && height.compareTo(BigDecimal.ZERO) > 0) {
            bmi = weight.divide(height.pow(2), 1, RoundingMode.HALF_UP).toString();
        }

        // 3. 查询系统可用课程列表（用于AI选择）
        cachedCourses = courseService.getHomePageCourseCards(50);
        StringBuilder coursesJson = new StringBuilder("[");
        for (int i = 0; i < cachedCourses.size(); i++) {
            CourseCardVO c = cachedCourses.get(i);
            coursesJson.append(String.format(
                "{\"id\":%d,\"name\":\"%s\",\"coverImage\":\"%s\",\"description\":\"%s\",\"duration\":%d,\"category\":\"%s\"}",
                c.getId(),
                escapeJson(c.getName()),
                escapeJson(c.getImage()),
                escapeJson(c.getDesc()),
                c.getDuration() != null ? c.getDuration() : 45,
                escapeJson(c.getCategory())
            ));
            if (i < cachedCourses.size() - 1) {
                coursesJson.append(",");
            }
        }
        coursesJson.append("]");

        // 4. 查询系统可用器械列表（只查正常状态的）
        com.fitness.modules.equipment.model.dto.EquipmentQueryDTO equipQuery =
                new com.fitness.modules.equipment.model.dto.EquipmentQueryDTO();
        equipQuery.setStatus(1);
        equipQuery.setPageSize(30);
        cachedEquipment = equipmentService.getEquipmentList(equipQuery).getRecords();
        StringBuilder equipmentJson = new StringBuilder("[");
        for (int i = 0; i < Math.min(cachedEquipment.size(), 30); i++) {
            EquipmentVO e = cachedEquipment.get(i);
            equipmentJson.append(String.format(
                "{\"id\":%d,\"name\":\"%s\",\"image\":\"%s\"}",
                e.getId(),
                escapeJson(e.getEquipmentName()),
                escapeJson(e.getImageUrl())
            ));
            if (i < Math.min(cachedEquipment.size(), 30) - 1) {
                equipmentJson.append(",");
            }
        }
        equipmentJson.append("]");

        // 5. 构建Prompt变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("height", height != null ? height.intValue() : 175);
        variables.put("weight", weight != null ? weight.intValue() : 70);
        variables.put("bmi", bmi);
        variables.put("age", profile.getAge() != null ? profile.getAge() : 25);

        // 转换健身目标枚举值为中文
        String goalText = convertGoalToChinese(profile.getFitnessGoal());
        variables.put("goal", goalText);

        // 转换经验等级为中文
        String expText = convertExperienceToChinese(profile.getExperience());
        variables.put("experience", expText);
        variables.put("availableCourses", coursesJson.toString());
        variables.put("availableEquipment", equipmentJson.toString());

        // 6. 调用AI生成计划
        FitnessPlanResponseDTO aiResponse;
        try {
            aiResponse = aiService.generateFitnessPlanFromProfile(variables);
        } catch (Exception e) {
            log.error("AI生成计划失败: userId={}, error={}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.AI_GENERATE_ERROR);
        }

        // 7. 解析DTO（强制约束llm返回格式）响应并校验替换假数据
        Map<String, Object> result = parseAndValidateAiResponse(aiResponse, bmi,
                goalText, expText);

        log.info("从个人档案生成健身计划成功: userId={}", userId);
        return result;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String convertGoalToChinese(String goal) {
        if (goal == null) return "增肌塑形";
        switch (goal) {
            case "WEIGHT_LOSS": return "减脂";
            case "MUSCLE_GAIN": return "增肌";
            case "BODY_SHAPING": return "塑形";
            case "ENDURANCE": return "增强体能";
            case "HEALTH": return "保持健康";
            default: return goal;
        }
    }

    private String convertExperienceToChinese(String experience) {
        if (experience == null) return "中级";
        switch (experience) {
            case "BEGINNER": return "初级";
            case "INTERMEDIATE": return "中级";
            case "ADVANCED": return "高级";
            default: return experience;
        }
    }

    /**
     * 解析并校验AI返回的DTO响应，替换假数据为真实数据
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAndValidateAiResponse(FitnessPlanResponseDTO aiResponse, String bmi, String goal, String experience) {
        try {
            Map<String, Object> planData = new HashMap<>();
            
            planData.put("subtitle", aiResponse.getSubtitle());
            
            Map<String, Object> userInfo = new HashMap<>();
            if (aiResponse.getUserInfo() != null) {
                userInfo.put("height", aiResponse.getUserInfo().getHeight());
                userInfo.put("weight", aiResponse.getUserInfo().getWeight());
                userInfo.put("bmi", bmi);
                userInfo.put("goal", goal != null ? goal : "增肌塑形");
                userInfo.put("intensity", experience != null ? experience : "中等");
            }
            planData.put("userInfo", userInfo);

            List<Map<String, Object>> weeklyPlanList = new ArrayList<>();
            if (aiResponse.getWeeklyPlan() != null) {
                for (FitnessPlanResponseDTO.DayPlanDTO dayPlanDTO : aiResponse.getWeeklyPlan()) {
                    Map<String, Object> dayPlan = convertDayPlanDTOToMap(dayPlanDTO);
                    validateAndReplaceDayPlan(dayPlan);
                    weeklyPlanList.add(dayPlan);
                }
            }
            planData.put("weeklyPlan", weeklyPlanList);

            return planData;
        } catch (Exception e) {
            log.warn("解析AI DTO失败，使用默认计划: error={}", e.getMessage());
            return generateDefaultPlanData(bmi, goal, experience);
        }
    }

    /**
     * 将 DayPlanDTO 转换为 Map
     */
    private Map<String, Object> convertDayPlanDTOToMap(FitnessPlanResponseDTO.DayPlanDTO dayPlanDTO) {
        Map<String, Object> dayPlan = new HashMap<>();
        dayPlan.put("dayName", dayPlanDTO.getDayName());
        dayPlan.put("focus", dayPlanDTO.getFocus());
        dayPlan.put("tips", dayPlanDTO.getTips());

        if (dayPlanDTO.getCourse() != null) {
            FitnessPlanResponseDTO.CourseDTO courseDTO = dayPlanDTO.getCourse();
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("name", courseDTO.getName());
            courseMap.put("description", courseDTO.getDescription());
            courseMap.put("coverImage", courseDTO.getCoverImage());
            courseMap.put("duration", courseDTO.getDuration());
            courseMap.put("id", courseDTO.getCourseId());
            dayPlan.put("course", courseMap);
        }

        if (dayPlanDTO.getEquipment() != null) {
            List<Map<String, Object>> equipmentList = new ArrayList<>();
            for (FitnessPlanResponseDTO.EquipmentDTO equipDTO : dayPlanDTO.getEquipment()) {
                Map<String, Object> equipMap = new HashMap<>();
                equipMap.put("name", equipDTO.getName());
                equipMap.put("image", equipDTO.getImage());
                equipmentList.add(equipMap);
            }
            dayPlan.put("equipment", equipmentList);
        }

        if (dayPlanDTO.getExercises() != null) {
            List<Map<String, Object>> exercisesList = new ArrayList<>();
            for (FitnessPlanResponseDTO.ExerciseDTO exerciseDTO : dayPlanDTO.getExercises()) {
                Map<String, Object> exerciseMap = new HashMap<>();
                exerciseMap.put("name", exerciseDTO.getName());
                exerciseMap.put("sets", exerciseDTO.getSets());
                exerciseMap.put("reps", exerciseDTO.getReps());
                exerciseMap.put("restSeconds", exerciseDTO.getRestSeconds());
                exercisesList.add(exerciseMap);
            }
            dayPlan.put("exercises", exercisesList);
        }

        return dayPlan;
    }

    /**
     * 校验并替换每天的计划数据
     */
    @SuppressWarnings("unchecked")
    private void validateAndReplaceDayPlan(Map<String, Object> dayPlan) {
        String focus = (String) dayPlan.getOrDefault("focus", "");

        // 1. 校验并替换课程数据
        if (dayPlan.containsKey("course") && dayPlan.get("course") != null) {
            Map<String, Object> course = (Map<String, Object>) dayPlan.get("course");
            String courseName = (String) course.getOrDefault("name", "");

            // 在缓存的课程列表中查找匹配的课程
            CourseCardVO matchedCourse = findMatchingCourse(courseName, focus);
            if (matchedCourse != null) {
                course.put("name", matchedCourse.getName());
                course.put("coverImage", matchedCourse.getImage());
                course.put("description", matchedCourse.getDesc());
                course.put("duration", matchedCourse.getDuration());
                course.put("id", matchedCourse.getId());
                log.debug("替换课程: {} -> {}", courseName, matchedCourse.getName());
            } else {
                // 如果没有匹配的课程，随机选择一个相关的课程
                CourseCardVO randomCourse = findRandomCourseByFocus(focus);
                if (randomCourse != null) {
                    course.put("name", randomCourse.getName());
                    course.put("coverImage", randomCourse.getImage());
                    course.put("description", randomCourse.getDesc());
                    course.put("duration", randomCourse.getDuration());
                    course.put("id", randomCourse.getId());
                    log.debug("使用随机课程替换: {} -> {}", courseName, randomCourse.getName());
                }
            }
        }

        // 2. 校验并替换器械数据
        if (dayPlan.containsKey("equipment") && dayPlan.get("equipment") instanceof List) {
            List<Map<String, Object>> equipmentList = (List<Map<String, Object>>) dayPlan.get("equipment");
            List<Map<String, Object>> validatedEquipment = new ArrayList<>();

            for (Map<String, Object> equip : equipmentList) {
                String equipName = (String) equip.getOrDefault("name", "");

                // 在缓存的器械列表中查找匹配的器械
                EquipmentVO matchedEquip = findMatchingEquipment(equipName, focus);
                if (matchedEquip != null) {
                    Map<String, Object> validEquip = new HashMap<>();
                    validEquip.put("name", matchedEquip.getEquipmentName());
                    validEquip.put("image", matchedEquip.getImageUrl());
                    validatedEquipment.add(validEquip);
                    log.debug("替换器械: {} -> {}", equipName, matchedEquip.getEquipmentName());
                } else {
                    // 如果没有匹配的器械，尝试找相关的器械
                    EquipmentVO randomEquip = findRandomEquipmentByFocus(focus);
                    if (randomEquip != null) {
                        Map<String, Object> validEquip = new HashMap<>();
                        validEquip.put("name", randomEquip.getEquipmentName());
                        validEquip.put("image", randomEquip.getImageUrl());
                        validatedEquipment.add(validEquip);
                        log.debug("使用随机器械替换: {} -> {}", equipName, randomEquip.getEquipmentName());
                    }
                }
            }

            // 如果没有任何有效器械，添加默认器械
            if (validatedEquipment.isEmpty()) {
                EquipmentVO defaultEquip = findRandomEquipmentByFocus(focus);
                if (defaultEquip != null) {
                    Map<String, Object> validEquip = new HashMap<>();
                    validEquip.put("name", defaultEquip.getEquipmentName());
                    validEquip.put("image", defaultEquip.getImageUrl());
                    validatedEquipment.add(validEquip);
                }
            }

            dayPlan.put("equipment", validatedEquipment);
        }
    }

    /**
     * 根据名称查找匹配的课程
     */
    private CourseCardVO findMatchingCourse(String courseName, String focus) {
        if (cachedCourses == null || cachedCourses.isEmpty()) return null;

        // 先尝试精确匹配
        for (CourseCardVO course : cachedCourses) {
            if (course.getName() != null && course.getName().equals(courseName)) {
                return course;
            }
        }

        // 再尝试包含匹配
        for (CourseCardVO course : cachedCourses) {
            if (course.getName() != null && courseName != null &&
                    (course.getName().contains(courseName) || courseName.contains(course.getName()))) {
                return course;
            }
        }

        return null;
    }

    /**
     * 根据训练重点随机选择相关课程
     */
    private CourseCardVO findRandomCourseByFocus(String focus) {
        if (cachedCourses == null || cachedCourses.isEmpty()) return null;

        // 根据focus关键词匹配课程
        List<CourseCardVO> matchedCourses = new ArrayList<>();
        String focusLower = focus.toLowerCase();

        for (CourseCardVO course : cachedCourses) {
            String category = course.getCategory() != null ? course.getCategory().toLowerCase() : "";
            String name = course.getName() != null ? course.getName().toLowerCase() : "";
            String desc = course.getDesc() != null ? course.getDesc().toLowerCase() : "";

            // 根据训练重点匹配相关课程
            if (matchesFocus(focusLower, category, name, desc)) {
                matchedCourses.add(course);
            }
        }

        // 如果没有匹配到，返回随机课程
        if (matchedCourses.isEmpty()) {
            int randomIndex = (int) (Math.random() * cachedCourses.size());
            return cachedCourses.get(randomIndex);
        }

        // 随机返回一个匹配的课程
        int randomIndex = (int) (Math.random() * matchedCourses.size());
        return matchedCourses.get(randomIndex);
    }

    /**
     * 判断课程是否匹配训练重点
     */
    private boolean matchesFocus(String focus, String category, String name, String desc) {
        // 胸肌/胸部训练
        if (focus.contains("胸") || focus.contains("chest")) {
            return category.contains("胸") || name.contains("胸") || desc.contains("胸") ||
                   category.contains("chest") || name.contains("chest") || desc.contains("chest");
        }
        // 背部训练
        if (focus.contains("背") || focus.contains("back")) {
            return category.contains("背") || name.contains("背") || desc.contains("背") ||
                   category.contains("back") || name.contains("back") || desc.contains("back");
        }
        // 腿部训练
        if (focus.contains("腿") || focus.contains("leg")) {
            return category.contains("腿") || name.contains("腿") || desc.contains("腿") ||
                   category.contains("leg") || name.contains("leg") || desc.contains("leg");
        }
        // 肩部训练
        if (focus.contains("肩") || focus.contains("shoulder")) {
            return category.contains("肩") || name.contains("肩") || desc.contains("肩") ||
                   category.contains("shoulder") || name.contains("shoulder") || desc.contains("shoulder");
        }
        // 手臂训练
        if (focus.contains("臂") || focus.contains("arm") || focus.contains("二头") || focus.contains("三头")) {
            return category.contains("臂") || name.contains("臂") || desc.contains("臂") ||
                   category.contains("arm") || name.contains("arm") || desc.contains("arm");
        }
        // 核心/腹肌训练
        if (focus.contains("腹") || focus.contains("核心") || focus.contains("core") || focus.contains("abs")) {
            return category.contains("腹") || name.contains("腹") || desc.contains("腹") ||
                   category.contains("核心") || name.contains("核心") || desc.contains("核心") ||
                   category.contains("core") || name.contains("core") || desc.contains("core");
        }
        // 有氧/心肺训练
        if (focus.contains("有氧") || focus.contains("心肺") || focus.contains("cardio")) {
            return category.contains("有氧") || name.contains("有氧") || desc.contains("有氧") ||
                   category.contains("心肺") || name.contains("心肺") || desc.contains("心肺") ||
                   category.contains("cardio") || name.contains("cardio");
        }
        // 全身训练
        if (focus.contains("全身") || focus.contains("whole") || focus.contains("full")) {
            return category.contains("全身") || name.contains("全身") || desc.contains("全身") ||
                   category.contains("综合") || name.contains("综合");
        }
        // 默认匹配
        return true;
    }

    /**
     * 根据名称查找匹配的器械
     */
    private EquipmentVO findMatchingEquipment(String equipName, String focus) {
        if (cachedEquipment == null || cachedEquipment.isEmpty()) return null;

        // 先尝试精确匹配
        for (EquipmentVO equip : cachedEquipment) {
            if (equip.getEquipmentName() != null && equip.getEquipmentName().equals(equipName)) {
                return equip;
            }
        }

        // 再尝试包含匹配
        for (EquipmentVO equip : cachedEquipment) {
            if (equip.getEquipmentName() != null && equipName != null &&
                    (equip.getEquipmentName().contains(equipName) || equipName.contains(equip.getEquipmentName()))) {
                return equip;
            }
        }

        return null;
    }

    /**
     * 根据训练重点随机选择相关器械
     */
    private EquipmentVO findRandomEquipmentByFocus(String focus) {
        if (cachedEquipment == null || cachedEquipment.isEmpty()) return null;

        // 根据focus关键词匹配器械
        List<EquipmentVO> matchedEquipment = new ArrayList<>();
        String focusLower = focus.toLowerCase();

        for (EquipmentVO equip : cachedEquipment) {
            String name = equip.getEquipmentName() != null ? equip.getEquipmentName().toLowerCase() : "";
            String desc = equip.getDescription() != null ? equip.getDescription().toLowerCase() : "";

            // 根据训练重点匹配相关器械
            if (matchesEquipmentFocus(focusLower, name, desc)) {
                matchedEquipment.add(equip);
            }
        }

        // 如果没有匹配到，返回随机器械
        if (matchedEquipment.isEmpty()) {
            int randomIndex = (int) (Math.random() * cachedEquipment.size());
            return cachedEquipment.get(randomIndex);
        }

        // 随机返回一个匹配的器械
        int randomIndex = (int) (Math.random() * matchedEquipment.size());
        return matchedEquipment.get(randomIndex);
    }

    /**
     * 判断器械是否匹配训练重点
     */
    private boolean matchesEquipmentFocus(String focus, String name, String desc) {
        // 胸肌/胸部训练
        if (focus.contains("胸") || focus.contains("chest")) {
            return name.contains("胸") || desc.contains("胸") ||
                   name.contains("卧推") || desc.contains("卧推") ||
                   name.contains("chest") || desc.contains("chest") ||
                   name.contains("bench") || desc.contains("bench");
        }
        // 背部训练
        if (focus.contains("背") || focus.contains("back")) {
            return name.contains("背") || desc.contains("背") ||
                   name.contains("划船") || desc.contains("划船") ||
                   name.contains("引体") || desc.contains("引体") ||
                   name.contains("back") || desc.contains("back") ||
                   name.contains("row") || desc.contains("row");
        }
        // 腿部训练
        if (focus.contains("腿") || focus.contains("leg")) {
            return name.contains("腿") || desc.contains("腿") ||
                   name.contains("深蹲") || desc.contains("深蹲") ||
                   name.contains("leg") || desc.contains("leg") ||
                   name.contains("squat") || desc.contains("squat");
        }
        // 肩部训练
        if (focus.contains("肩") || focus.contains("shoulder")) {
            return name.contains("肩") || desc.contains("肩") ||
                   name.contains("shoulder") || desc.contains("shoulder");
        }
        // 手臂训练
        if (focus.contains("臂") || focus.contains("arm") || focus.contains("二头") || focus.contains("三头")) {
            return name.contains("臂") || desc.contains("臂") ||
                   name.contains("哑铃") || desc.contains("哑铃") ||
                   name.contains("杠铃") || desc.contains("杠铃") ||
                   name.contains("arm") || desc.contains("arm");
        }
        // 核心/腹肌训练
        if (focus.contains("腹") || focus.contains("核心") || focus.contains("core") || focus.contains("abs")) {
            return name.contains("腹") || desc.contains("腹") ||
                   name.contains("核心") || desc.contains("核心") ||
                   name.contains("瑜伽") || desc.contains("瑜伽") ||
                   name.contains("core") || desc.contains("core");
        }
        // 有氧/心肺训练
        if (focus.contains("有氧") || focus.contains("心肺") || focus.contains("cardio")) {
            return name.contains("跑步") || desc.contains("跑步") ||
                   name.contains("单车") || desc.contains("单车") ||
                   name.contains("椭圆") || desc.contains("椭圆") ||
                   name.contains("有氧") || desc.contains("有氧") ||
                   name.contains("cardio") || desc.contains("cardio");
        }
        // 默认匹配
        return true;
    }

    /**
     * 生成默认计划数据（当AI解析失败时使用）
     */
    private Map<String, Object> generateDefaultPlanData(String bmi, String goal, String experience) {
        Map<String, Object> result = new HashMap<>();

        // 用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("height", "175cm");
        userInfo.put("weight", "70kg");
        userInfo.put("bmi", bmi);
        userInfo.put("goal", goal != null ? goal : "增肌塑形");
        userInfo.put("intensity", experience != null ? experience : "中等");
        result.put("userInfo", userInfo);

        // 周计划
        List<Map<String, Object>> weeklyPlan = new ArrayList<>();

        // 周一 - 胸部训练
        weeklyPlan.add(createDefaultDayPlan("周一", "胸部训练", "胸肌", 1));
        // 周二 - 有氧运动
        weeklyPlan.add(createDefaultDayPlan("周二", "有氧运动", "心肺", 2));
        // 周三 - 背部训练
        weeklyPlan.add(createDefaultDayPlan("周三", "背部训练", "背部", 3));
        // 周四 - 休息日
        weeklyPlan.add(createDefaultDayPlan("周四", "休息日", "恢复", 4));
        // 周五 - 腿部训练
        weeklyPlan.add(createDefaultDayPlan("周五", "腿部训练", "腿部", 5));
        // 周六 - 肩部训练
        weeklyPlan.add(createDefaultDayPlan("周六", "肩部训练", "肩部", 6));
        // 周日 - 全身训练
        weeklyPlan.add(createDefaultDayPlan("周日", "全身训练", "全身", 7));

        result.put("weeklyPlan", weeklyPlan);
        return result;
    }

    /**
     * 创建默认的每日计划
     */
    private Map<String, Object> createDefaultDayPlan(String day, String focus, String focusKey, int dayIndex) {
        Map<String, Object> dayPlan = new HashMap<>();
        dayPlan.put("day", day);
        dayPlan.put("focus", focus);

        // 如果是休息日
        if (focus.contains("休息")) {
            dayPlan.put("course", null);
            dayPlan.put("equipment", new ArrayList<>());
            dayPlan.put("exercises", new ArrayList<>());
            return dayPlan;
        }

        // 查找匹配的课程
        CourseCardVO course = findRandomCourseByFocus(focusKey);
        if (course != null) {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id", course.getId());
            courseMap.put("name", course.getName());
            courseMap.put("coverImage", course.getImage());
            courseMap.put("description", course.getDesc());
            courseMap.put("duration", course.getDuration());
            dayPlan.put("course", courseMap);
        }

        // 查找匹配的器械
        List<Map<String, Object>> equipmentList = new ArrayList<>();
        EquipmentVO equip1 = findRandomEquipmentByFocus(focusKey);
        if (equip1 != null) {
            Map<String, Object> equipMap = new HashMap<>();
            equipMap.put("name", equip1.getEquipmentName());
            equipMap.put("image", equip1.getImageUrl());
            equipmentList.add(equipMap);
        }
        EquipmentVO equip2 = findRandomEquipmentByFocus(focusKey);
        if (equip2 != null && !equip2.getId().equals(equip1 != null ? equip1.getId() : null)) {
            Map<String, Object> equipMap = new HashMap<>();
            equipMap.put("name", equip2.getEquipmentName());
            equipMap.put("image", equip2.getImageUrl());
            equipmentList.add(equipMap);
        }
        dayPlan.put("equipment", equipmentList);

        // 默认训练动作
        List<Map<String, Object>> exercises = new ArrayList<>();
        exercises.add(createExercise("热身", "5-10分钟", "轻度有氧运动"));
        exercises.add(createExercise("主训练", "30-45分钟", focus + "专项训练"));
        exercises.add(createExercise("拉伸放松", "5-10分钟", "静态拉伸"));
        dayPlan.put("exercises", exercises);

        return dayPlan;
    }

    /**
     * 创建训练动作
     */
    private Map<String, Object> createExercise(String name, String duration, String description) {
        Map<String, Object> exercise = new HashMap<>();
        exercise.put("name", name);
        exercise.put("duration", duration);
        exercise.put("description", description);
        return exercise;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generatePlan(Long userId, PlanGenerateDTO dto) {
        log.info("生成健身计划: userId={}, goal={}", userId, dto.getGoal());

        // 1. 创建计划主表
        FitnessPlan plan = new FitnessPlan();
        plan.setUserId(userId);
        plan.setPlanName(dto.getPlanName());
        plan.setGoal(dto.getGoal());
        plan.setDuration(dto.getDuration());
        plan.setLevel(dto.getLevel());
        plan.setStatus(1);
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());

        fitnessPlanMapper.insert(plan);

        // 2. 调用AI生成计划详情
        Map<String, Object> aiResult = generatePlanFromProfile(userId);

        // 3. 保存计划详情
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> weeklyPlan = (List<Map<String, Object>>) aiResult.get("weeklyPlan");
        if (weeklyPlan != null) {
            for (int i = 0; i < weeklyPlan.size(); i++) {
                Map<String, Object> dayPlan = weeklyPlan.get(i);
                FitnessPlanDetail detail = new FitnessPlanDetail();
                detail.setPlanId(plan.getId());
                detail.setDayIndex(i + 1);
                detail.setDayName((String) dayPlan.get("day"));
                detail.setFocus((String) dayPlan.get("focus"));

                // 保存课程信息
                @SuppressWarnings("unchecked")
                Map<String, Object> course = (Map<String, Object>) dayPlan.get("course");
                if (course != null) {
                    detail.setCourseId(course.get("id") != null ? Long.valueOf(course.get("id").toString()) : null);
                    detail.setCourseName((String) course.get("name"));
                }

                // 保存器械信息（JSON格式）
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> equipment = (List<Map<String, Object>>) dayPlan.get("equipment");
                if (equipment != null && !equipment.isEmpty()) {
                    try {
                        detail.setEquipmentJson(objectMapper.writeValueAsString(equipment));
                    } catch (JsonProcessingException e) {
                        log.warn("序列化器械信息失败: {}", e.getMessage());
                    }
                }

                // 保存训练动作（JSON格式）
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> exercises = (List<Map<String, Object>>) dayPlan.get("exercises");
                if (exercises != null && !exercises.isEmpty()) {
                    try {
                        detail.setExercisesJson(objectMapper.writeValueAsString(exercises));
                    } catch (JsonProcessingException e) {
                        log.warn("序列化训练动作失败: {}", e.getMessage());
                    }
                }

                detail.setCreateTime(LocalDateTime.now());
                fitnessPlanDetailMapper.insert(detail);
            }
        }

        log.info("生成健身计划成功: planId={}", plan.getId());
        return plan.getId();
    }

    @Override
    public PlanVO getPlanDetail(Long userId, Long planId) {
        FitnessPlan plan = fitnessPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        PlanVO vo = new PlanVO();
        BeanUtil.copyProperties(plan, vo);

        // 查询计划详情并转换
        List<FitnessPlanDetail> detailEntities = fitnessPlanDetailMapper.selectByPlanId(planId);
        List<PlanDetailVO> details = convertToDetailVOList(detailEntities);
        vo.setDetails(details);

        return vo;
    }

    /**
     * 将实体列表转换为VO列表
     */
    private List<PlanDetailVO> convertToDetailVOList(List<FitnessPlanDetail> entities) {
        List<PlanDetailVO> result = new ArrayList<>();
        if (entities == null) return result;

        for (FitnessPlanDetail entity : entities) {
            PlanDetailVO vo = new PlanDetailVO();
            vo.setId(entity.getId());
            vo.setDayIndex(entity.getDayIndex());
            vo.setDayName(entity.getDayName());
            vo.setFocus(entity.getFocus());
            vo.setCourseId(entity.getCourseId());
            vo.setCourseName(entity.getCourseName());
            vo.setDuration(entity.getDuration());
            vo.setNotes(entity.getNotes());

            // 解析器械JSON
            if (StrUtil.isNotBlank(entity.getEquipmentJson())) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> equipment = objectMapper.readValue(entity.getEquipmentJson(), List.class);
                    vo.setEquipment(equipment);
                } catch (JsonProcessingException e) {
                    log.warn("解析器械JSON失败: {}", e.getMessage());
                    vo.setEquipment(new ArrayList<>());
                }
            } else {
                vo.setEquipment(new ArrayList<>());
            }

            // 解析训练动作JSON
            if (StrUtil.isNotBlank(entity.getExercisesJson())) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> exercises = objectMapper.readValue(entity.getExercisesJson(), List.class);
                    vo.setExercises(exercises);
                } catch (JsonProcessingException e) {
                    log.warn("解析训练动作JSON失败: {}", e.getMessage());
                    vo.setExercises(new ArrayList<>());
                }
            } else {
                vo.setExercises(new ArrayList<>());
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<PlanVO> getPlanList(Long userId) {
        List<FitnessPlan> plans = fitnessPlanMapper.selectByUserId(userId);
        List<PlanVO> result = new ArrayList<>();

        for (FitnessPlan plan : plans) {
            PlanVO vo = new PlanVO();
            BeanUtil.copyProperties(plan, vo);
            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long userId, Long planId) {
        FitnessPlan plan = fitnessPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 删除详情 - 使用 MyBatis-Plus 的 delete 方法
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FitnessPlanDetail> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(FitnessPlanDetail::getPlanId, planId);
        fitnessPlanDetailMapper.delete(wrapper);
        // 删除主表
        fitnessPlanMapper.deleteById(planId);

        log.info("删除健身计划: planId={}", planId);
    }
}