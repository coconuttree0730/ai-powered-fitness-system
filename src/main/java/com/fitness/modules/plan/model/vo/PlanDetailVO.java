package com.fitness.modules.plan.model.vo;

import lombok.Data;

/**
 * 计划详情VO
 */
@Data
public class PlanDetailVO {

    /**
     * 详情ID
     */
    private Long id;

    /**
     * 星期几(1-7)
     */
    private Integer dayOfWeek;

    /**
     * 运动名称
     */
    private String exerciseName;

    /**
     * 组数
     */
    private Integer sets;

    /**
     * 每组次数
     */
    private Integer reps;

    /**
     * 持续时间(分钟)
     */
    private Integer duration;

    /**
     * 备注
     */
    private String notes;
}
