package com.fitness.modules.plan.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import com.fitness.common.mybatis.JsonbTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "fitness_plan", autoResultMap = true)
public class FitnessPlan extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("plan_name")
    private String planName;

    @TableField("goal")
    private String goal;

    @TableField("duration")
    private Integer duration;

    @TableField("level")
    private String level;

    @TableField("status")
    private Integer status;

    @TableField("height")
    private BigDecimal height;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("age")
    private Integer age;

    @TableField("gender")
    private String gender;

    @TableField("experience")
    private String experience;

    @TableField("fitness_goal")
    private String fitnessGoal;

    @TableField(value = "plan_data_json", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> planDataJson;
}
