package com.fitness.modules.plan.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PlanVO {

    private Long id;

    private String planName;

    private String goal;

    private Integer duration;

    private String level;

    private Integer status;

    private BigDecimal height;

    private BigDecimal weight;

    private Integer age;

    private String gender;

    private String experience;

    private String fitnessGoal;

    private Map<String, Object> planDataJson;

    private LocalDateTime createTime;

    private List<PlanDetailVO> details;
}
