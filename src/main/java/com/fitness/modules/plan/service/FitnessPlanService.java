package com.fitness.modules.plan.service;

import com.fitness.modules.plan.model.dto.PlanGenerateDTO;
import com.fitness.modules.plan.model.vo.PlanVO;

import java.util.List;

/**
 * 健身计划服务接口
 */
public interface FitnessPlanService {

    /**
     * 生成健身计划
     *
     * @param userId 用户ID
     * @param dto    生成计划请求
     * @return 生成的计划ID
     */
    Long generatePlan(Long userId, PlanGenerateDTO dto);

    /**
     * 获取用户的计划列表
     *
     * @param userId 用户ID
     * @return 计划列表
     */
    List<PlanVO> getPlanList(Long userId);

    /**
     * 获取计划详情
     *
     * @param userId 用户ID
     * @param planId 计划ID
     * @return 计划详情
     */
    PlanVO getPlanDetail(Long userId, Long planId);

    /**
     * 删除计划
     *
     * @param userId 用户ID
     * @param planId 计划ID
     */
    void deletePlan(Long userId, Long planId);
}
