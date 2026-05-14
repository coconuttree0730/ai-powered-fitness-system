package com.fitness.integration.ai.support;

import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiFitnessPlanValidator {

    public void validateFitnessPlan(FitnessPlanResponseDTO response) {
        if (response == null) {
            throw new IllegalArgumentException("生成的计划为空");
        }
        if (response.getWeeklyPlan() == null) {
            throw new IllegalArgumentException("生成的计划缺少 weeklyPlan 字段");
        }
        if (response.getWeeklyPlan().size() > 7) {
            log.warn("生成的计划天数超过 7 天，截断到 7 天");
            response.setWeeklyPlan(response.getWeeklyPlan().subList(0, 7));
        }
        if (response.getWeeklyPlan().size() != 7) {
            throw new IllegalArgumentException("生成的计划天数不正确: " + response.getWeeklyPlan().size());
        }
    }
}
