package com.fitness.modules.plan.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.plan.model.dto.PlanGenerateDTO;
import com.fitness.modules.plan.model.dto.PlanGenerationTask;
import com.fitness.modules.plan.model.dto.SaveFitnessPlanDTO;
import com.fitness.modules.plan.model.vo.PlanVO;
import com.fitness.modules.plan.service.FitnessPlanService;
import com.fitness.modules.plan.service.PlanGenerationTaskManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 健身计划控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class FitnessPlanController {

    private final FitnessPlanService fitnessPlanService;
    private final PlanGenerationTaskManager taskManager;

    /**
     * 生成健身计划————（从个人档案自动获取数据）————
     *
     * @return 结构化的健身计划JSON数据
     */
    @PostMapping("/generate-from-profile")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Map<String, Object>> generatePlanFromProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("从个人档案生成健身计划请求: userId={}", userId);
        Map<String, Object> planData = fitnessPlanService.generatePlanFromProfile(userId);
        return Result.success(planData);
    }

    /**
     * 生成健身计划（手动选择参数 - 旧版兼容）：前端select 选择器选择输入类型，输入参数
     *
     * @param dto 生成计划请求
     * @return 生成的计划ID
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Long> generatePlan(@Valid @RequestBody PlanGenerateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("生成健身计划请求: userId={}, goal={}, bodyPart={}, experience={}",
                userId, dto.getGoal(), dto.getBodyPart(), dto.getExperience());
        Long planId = fitnessPlanService.generatePlan(userId, dto);
        return Result.success(planId);
    }

    /**
     * 获取我的计划列表：用户健身计划表的显示
     *
     * @return 计划列表
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<List<PlanVO>> getMyPlans() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取我的计划列表: userId={}", userId);
        List<PlanVO> plans = fitnessPlanService.getPlanList(userId);
        return Result.success(plans);
    }

    /**
     * 获取计划详情
     *
     * @param planId 计划ID
     * @return 计划详情
     */
    @GetMapping("/{planId}")
    @PreAuthorize("isAuthenticated()")
    public Result<PlanVO> getPlanDetail(@PathVariable Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取计划详情: userId={}, planId={}", userId, planId);
        PlanVO plan = fitnessPlanService.getPlanDetail(userId, planId);
        return Result.success(plan);
    }

    /**
     * 删除计划
     *
     * @param planId 计划ID
     * @return 操作结果
     */
    @DeleteMapping("/{planId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deletePlan(@PathVariable Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("删除计划: userId={}, planId={}", userId, planId);
        fitnessPlanService.deletePlan(userId, planId);
        return Result.success();
    }

    /**
     * 异步生成健身计划（从个人档案）
     * 立即返回任务ID，前端轮询获取结果
     */
    @PostMapping("/generate-from-profile/async")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Map<String, String>> generatePlanFromProfileAsync() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("异步生成健身计划请求: userId={}", userId);
        PlanGenerationTask task = taskManager.createTask();
        fitnessPlanService.executeAsyncGeneration(userId, task.getTaskId());
        Map<String, String> response = new java.util.HashMap<>();
        response.put("taskId", task.getTaskId());
        response.put("status", task.getStatus().name());
        return Result.success(response);
    }

    /**
     * 查询异步生成任务状态
     */
    @GetMapping("/generate-from-profile/async/{taskId}")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<PlanGenerationTask> getGenerationTaskStatus(@PathVariable String taskId) {
        PlanGenerationTask task = taskManager.getTask(taskId);
        if (task == null) {
            return Result.error(404, "任务不存在或已过期");
        }
        return Result.success(task);
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Long> savePlan(@Valid @RequestBody SaveFitnessPlanDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("保存健身计划: userId={}", userId);
        Long planId = fitnessPlanService.savePlan(userId, dto);
        return Result.success(planId);
    }
}
