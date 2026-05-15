package com.fitness.modules.plan.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 健身计划详情实体类
 * 对应 fitness_plan_detail 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fitness_plan_detail")
public class FitnessPlanDetail extends BaseEntity {

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
     * 天数索引(1-7)
     */
    @TableField("day_index")
    private Integer dayIndex;

    /**
     * 星期名称(周一、周二等)
     */
    @TableField("day_name")
    private String dayName;

    /**
     * 训练重点(胸部、背部等)
     */
    @TableField("focus")
    private String focus;

    /**
     * 课程ID
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 器械信息(JSON格式)
     */
    @TableField("equipment_json")
    private String equipmentJson;

    /**
     * 训练动作(JSON格式)
     */
    @TableField("exercises_json")
    private String exercisesJson;

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
}
