package com.fitness.modules.plan.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.mybatis.JsonbTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "fitness_plan", autoResultMap = true)
public class FitnessPlan {

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

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
