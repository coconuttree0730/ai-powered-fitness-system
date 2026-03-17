package com.fitness.modules.plan.service.impl;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.plan.mapper.FitnessPlanDetailMapper;
import com.fitness.modules.plan.mapper.FitnessPlanMapper;
import com.fitness.modules.plan.model.dto.PlanGenerateDTO;
import com.fitness.modules.plan.model.entity.FitnessPlan;
import com.fitness.modules.plan.model.entity.FitnessPlanDetail;
import com.fitness.modules.plan.model.vo.PlanDetailVO;
import com.fitness.modules.plan.model.vo.PlanVO;
import com.fitness.modules.plan.service.FitnessPlanService;
import com.fitness.modules.plan.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final UserProfileService userProfileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generatePlan(Long userId, PlanGenerateDTO dto) {
        // 检查用户是否已完善个人信息
        if (!userProfileService.isProfileComplete(userId)) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETE);
        }

        // 获取用户个人信息
        Map<String, Object> profile = userProfileService.getProfile(userId);
        BigDecimal height = (BigDecimal) profile.get("height");
        BigDecimal weight = (BigDecimal) profile.get("weight");
        Integer age = (Integer) profile.get("age");
        String experience = (String) profile.get("experience");

        // 调用AI服务生成计划
        String aiResponse;
        try {
            aiResponse = aiService.generateFitnessPlan(
                    dto.getGoal(),
                    dto.getBodyPart(),
                    experience,
                    height != null ? height.intValue() : null,
                    weight != null ? weight.intValue() : null,
                    age
            );
        } catch (Exception e) {
            log.error("AI生成计划失败: userId={}, error={}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.AI_GENERATE_ERROR);
        }

        // 解析AI返回结果
        List<PlanDetailVO> details = parseAIResponse(aiResponse);

        // 创建计划
        FitnessPlan plan = new FitnessPlan();
        plan.setUserId(userId);
        plan.setPlanName(generatePlanName(dto.getGoal(), dto.getBodyPart()));
        plan.setGoal(dto.getGoal());
        plan.setDuration(7); // 默认7天计划
        plan.setStatus(1); // 进行中
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        fitnessPlanMapper.insert(plan);

        // 保存计划详情
        int sortOrder = 0;
        for (PlanDetailVO detailVO : details) {
            FitnessPlanDetail detail = new FitnessPlanDetail();
            detail.setPlanId(plan.getId());
            detail.setDayOfWeek(detailVO.getDayOfWeek());
            detail.setExerciseName(detailVO.getExerciseName());
            detail.setSets(detailVO.getSets());
            detail.setReps(detailVO.getReps());
            detail.setDuration(detailVO.getDuration());
            detail.setNotes(detailVO.getNotes());
            detail.setSortOrder(sortOrder++);
            detail.setCreateTime(LocalDateTime.now());
            detail.setUpdateTime(LocalDateTime.now());
            fitnessPlanDetailMapper.insert(detail);
        }

        log.info("健身计划生成成功: userId={}, planId={}", userId, plan.getId());
        return plan.getId();
    }

    @Override
    public List<PlanVO> getPlanList(Long userId) {
        List<FitnessPlan> plans = fitnessPlanMapper.selectByUserId(userId);
        List<PlanVO> result = new ArrayList<>();

        for (FitnessPlan plan : plans) {
            PlanVO vo = convertToVO(plan);
            result.add(vo);
        }

        return result;
    }

    @Override
    public PlanVO getPlanDetail(Long userId, Long planId) {
        FitnessPlan plan = fitnessPlanMapper.selectById(planId);
        if (plan == null || plan.getDeleted()) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        // 检查是否属于当前用户
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        PlanVO vo = convertToVO(plan);

        // 查询详情列表
        List<FitnessPlanDetail> details = fitnessPlanDetailMapper.selectByPlanId(planId);
        List<PlanDetailVO> detailVOs = new ArrayList<>();
        for (FitnessPlanDetail detail : details) {
            PlanDetailVO detailVO = new PlanDetailVO();
            detailVO.setId(detail.getId());
            detailVO.setDayOfWeek(detail.getDayOfWeek());
            detailVO.setExerciseName(detail.getExerciseName());
            detailVO.setSets(detail.getSets());
            detailVO.setReps(detail.getReps());
            detailVO.setDuration(detail.getDuration());
            detailVO.setNotes(detail.getNotes());
            detailVOs.add(detailVO);
        }
        vo.setDetails(detailVOs);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long userId, Long planId) {
        FitnessPlan plan = fitnessPlanMapper.selectById(planId);
        if (plan == null || plan.getDeleted()) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        // 检查是否属于当前用户
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        fitnessPlanMapper.deleteById(planId);
        log.info("健身计划删除成功: userId={}, planId={}", userId, planId);
    }

    /**
     * 解析AI返回结果
     *
     * @param aiResponse AI返回的文本
     * @return 计划详情列表
     */
    private List<PlanDetailVO> parseAIResponse(String aiResponse) {
        List<PlanDetailVO> details = new ArrayList<>();

        // 按天分割内容
        String[] days = aiResponse.split("(?=第[一二三四五六七日1234567]天)");

        for (String dayContent : days) {
            if (dayContent.trim().isEmpty()) {
                continue;
            }

            // 提取星期几
            int dayOfWeek = extractDayOfWeek(dayContent);
            if (dayOfWeek == 0) {
                continue;
            }

            // 提取训练项目
            String[] exercises = dayContent.split("\\n");
            for (String exercise : exercises) {
                exercise = exercise.trim();
                if (exercise.isEmpty() || exercise.contains("第") && exercise.contains("天")) {
                    continue;
                }

                PlanDetailVO detail = parseExercise(exercise, dayOfWeek);
                if (detail != null) {
                    details.add(detail);
                }
            }
        }

        // 如果没有解析出任何内容，使用默认计划
        if (details.isEmpty()) {
            details = generateDefaultPlan();
        }

        return details;
    }

    /**
     * 提取星期几
     */
    private int extractDayOfWeek(String content) {
        if (content.contains("第一天") || content.contains("第1天")) return 1;
        if (content.contains("第二天") || content.contains("第2天")) return 2;
        if (content.contains("第三天") || content.contains("第3天")) return 3;
        if (content.contains("第四天") || content.contains("第4天")) return 4;
        if (content.contains("第五天") || content.contains("第5天")) return 5;
        if (content.contains("第六天") || content.contains("第6天")) return 6;
        if (content.contains("第七天") || content.contains("第7天") || content.contains("第七天") || content.contains("第日天"))
            return 7;
        return 0;
    }

    /**
     * 解析单个训练项目
     */
    private PlanDetailVO parseExercise(String exercise, int dayOfWeek) {
        PlanDetailVO detail = new PlanDetailVO();
        detail.setDayOfWeek(dayOfWeek);
        detail.setExerciseName(exercise);

        // 尝试提取组数和次数
        Pattern pattern = Pattern.compile("(\\d+)[组]\\s*[xX*]\\s*(\\d+)[次个]");
        Matcher matcher = pattern.matcher(exercise);
        if (matcher.find()) {
            detail.setSets(Integer.parseInt(matcher.group(1)));
            detail.setReps(Integer.parseInt(matcher.group(2)));
        } else {
            // 尝试提取持续时间
            Pattern durationPattern = Pattern.compile("(\\d+)[分钟min]");
            Matcher durationMatcher = durationPattern.matcher(exercise);
            if (durationMatcher.find()) {
                detail.setDuration(Integer.parseInt(durationMatcher.group(1)));
            }
        }

        // 提取备注
        if (exercise.contains("(")) {
            int start = exercise.indexOf("(");
            int end = exercise.indexOf(")");
            if (end > start) {
                detail.setNotes(exercise.substring(start + 1, end));
                detail.setExerciseName(exercise.substring(0, start).trim());
            }
        }

        return detail;
    }

    /**
     * 生成默认计划
     */
    private List<PlanDetailVO> generateDefaultPlan() {
        List<PlanDetailVO> details = new ArrayList<>();

        // 周一：胸部训练
        details.add(createDetail(1, "俯卧撑", 3, 15, null, "标准俯卧撑"));
        details.add(createDetail(1, "哑铃卧推", 4, 12, null, "中等重量"));
        details.add(createDetail(1, "飞鸟", 3, 15, null, "轻重量"));

        // 周二：背部训练
        details.add(createDetail(2, "引体向上", 3, 8, null, "或高位下拉"));
        details.add(createDetail(2, "哑铃划船", 4, 12, null, "中等重量"));
        details.add(createDetail(2, "硬拉", 3, 10, null, "注意姿势"));

        // 周三：有氧运动
        details.add(createDetail(3, "跑步", null, null, 30, "中等强度"));
        details.add(createDetail(3, "跳绳", null, null, 15, "分组进行"));

        // 周四：肩部训练
        details.add(createDetail(4, "哑铃推举", 4, 12, null, "中等重量"));
        details.add(createDetail(4, "侧平举", 3, 15, null, "轻重量"));
        details.add(createDetail(4, "前平举", 3, 12, null, "轻重量"));

        // 周五：腿部训练
        details.add(createDetail(5, "深蹲", 4, 15, null, "标准深蹲"));
        details.add(createDetail(5, "弓步蹲", 3, 12, null, "每条腿"));
        details.add(createDetail(5, "小腿提踵", 4, 20, null, "控制节奏"));

        // 周六：核心训练
        details.add(createDetail(6, "平板支撑", 3, null, 1, "每组1分钟"));
        details.add(createDetail(6, "卷腹", 4, 20, null, "控制速度"));
        details.add(createDetail(6, "俄罗斯转体", 3, 30, null, "每侧"));

        // 周日：休息或轻度活动
        details.add(createDetail(7, "瑜伽拉伸", null, null, 30, "放松恢复"));
        details.add(createDetail(7, "散步", null, null, 20, "轻松步行"));

        return details;
    }

    private PlanDetailVO createDetail(int dayOfWeek, String exerciseName, Integer sets, Integer reps, Integer duration, String notes) {
        PlanDetailVO detail = new PlanDetailVO();
        detail.setDayOfWeek(dayOfWeek);
        detail.setExerciseName(exerciseName);
        detail.setSets(sets);
        detail.setReps(reps);
        detail.setDuration(duration);
        detail.setNotes(notes);
        return detail;
    }

    /**
     * 生成计划名称
     */
    private String generatePlanName(String goal, String bodyPart) {
        return goal + "-" + bodyPart + "训练计划";
    }

    /**
     * 转换为VO
     */
    private PlanVO convertToVO(FitnessPlan plan) {
        PlanVO vo = new PlanVO();
        vo.setId(plan.getId());
        vo.setPlanName(plan.getPlanName());
        vo.setGoal(plan.getGoal());
        vo.setDuration(plan.getDuration());
        vo.setStatus(plan.getStatus());
        vo.setCreateTime(plan.getCreateTime());
        return vo;
    }
}
