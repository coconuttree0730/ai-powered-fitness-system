package com.fitness.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户健身档案实体类
 * 对应 user_fitness_profile 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_fitness_profile")
public class UserFitnessProfile extends BaseEntity {
    /**
     * 档案ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 身高(cm)
     */
    @TableField("height")
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    @TableField("weight")
    private BigDecimal weight;

    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;

    /**
     * 健身经验：BEGINNER-初学者, INTERMEDIATE-中级, ADVANCED-高级
     */
    @TableField("experience")
    private String experience;

    /**
     * 健身目标：WEIGHT_LOSS-减脂, MUSCLE_GAIN-增肌, BODY_SHAPING-塑形, ENDURANCE-增强体能, HEALTH-保持健康
     */
    @TableField("fitness_goal")
    private String fitnessGoal;

    /**
     * 性别：MALE-男, FEMALE-女
     */
    @TableField("gender")
    private String gender;

    /**
     * 专属教练ID
     */
    @TableField("private_coach_id")
    private Long privateCoachId;
}
