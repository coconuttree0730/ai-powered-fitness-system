package com.fitness.modules.plan.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.plan.model.dto.PlanGenerateDTO;
import com.fitness.modules.plan.model.vo.PlanVO;
import com.fitness.modules.plan.service.FitnessPlanService;
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

    /**
     * 生成健身计划（从个人档案自动获取数据）
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
     * 生成健身计划（手动选择参数 - 旧版兼容）
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
     * 获取我的计划列表
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
}
