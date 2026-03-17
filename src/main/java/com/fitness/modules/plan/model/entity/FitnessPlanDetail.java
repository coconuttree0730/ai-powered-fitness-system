package com.fitness.modules.plan.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健身计划详情实体类
 * 对应 fitness_plan_detail 表
 */
@Data
@TableName("fitness_plan_detail")
public class FitnessPlanDetail {

    /**
     * 详情ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 计划ID
     */
    @TableField("plan_id")
    private Long planId;

    /**
     * 星期几(1-7)
     */
    @TableField("day_of_week")
    private Integer dayOfWeek;

    /**
     * 运动名称
     */
    @TableField("exercise_name")
    private String exerciseName;

    /**
     * 组数
     */
    @TableField("sets")
    private Integer sets;

    /**
     * 每组次数
     */
    @TableField("reps")
    private Integer reps;

    /**
     * 持续时间(分钟)
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 备注
     */
    @TableField("notes")
    private String notes;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 软删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
