package com.fitness.modules.user.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户健身档案VO
 */
@Data
public class UserFitnessProfileVO {

    /**
     * 档案ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 身高(cm)
     */
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    private BigDecimal weight;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 健身经验：BEGINNER-初学者, INTERMEDIATE-中级, ADVANCED-高级
     */
    private String experience;

    /**
     * 健身目标：WEIGHT_LOSS-减脂, MUSCLE_GAIN-增肌, BODY_SHAPING-塑形, ENDURANCE-增强体能, HEALTH-保持健康
     */
    private String fitnessGoal;

    /**
     * 性别：MALE-男, FEMALE-女
     */
    private String gender;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
