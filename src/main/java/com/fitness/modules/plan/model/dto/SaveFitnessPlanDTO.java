package com.fitness.modules.plan.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class SaveFitnessPlanDTO {

    @NotNull(message = "计划数据不能为空")
    private Map<String, Object> planDataJson;

    private BigDecimal height;

    private BigDecimal weight;

    private Integer age;

    private String gender;

    private String experience;

    private String fitnessGoal;
}
