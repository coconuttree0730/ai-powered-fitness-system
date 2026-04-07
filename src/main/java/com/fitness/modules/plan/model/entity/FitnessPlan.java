package com.fitness.modules.plan.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健身计划实体类
 * 对应 fitness_plan 表
 */
@Data
@TableName("fitness_plan")
public class FitnessPlan {

    /**
     * 计划ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 计划名称
     */
    @TableField("plan_name")
    private String planName;

    /**
     * 健身目标
     */
    @TableField("goal")
    private String goal;

    /**
     * 计划周期(天)
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 难度等级: BEGINNER-初级, INTERMEDIATE-中级, ADVANCED-高级
     */
    @TableField("level")
    private String level;

    /**
     * 状态: 0-已停用, 1-进行中
     */
    @TableField("status")
    private Integer status;

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
    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
