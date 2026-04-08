package com.fitness.modules.plan.service;

import com.fitness.modules.plan.model.dto.PlanGenerateDTO;
import com.fitness.modules.plan.model.dto.SaveFitnessPlanDTO;
import com.fitness.modules.plan.model.vo.PlanVO;

import java.util.List;
import java.util.Map;

public interface FitnessPlanService {

    Long generatePlan(Long userId, PlanGenerateDTO dto);

    Map<String, Object> generatePlanFromProfile(Long userId);

    List<PlanVO> getPlanList(Long userId);

    PlanVO getPlanDetail(Long userId, Long planId);

    void deletePlan(Long userId, Long planId);

    Long savePlan(Long userId, SaveFitnessPlanDTO dto);
}
