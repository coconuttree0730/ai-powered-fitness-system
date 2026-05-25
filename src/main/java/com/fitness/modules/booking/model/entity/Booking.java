package com.fitness.modules.booking.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 预约实体类
 * 对应 fitness_booking 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fitness_booking")
public class Booking extends BaseEntity {

    /**
     * 预约ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 课程ID（关联课程模板）
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 课程实例ID（关联具体某一次上课）
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * 预约时间
     */
    @TableField("booking_time")
    private LocalDateTime bookingTime;

    /**
     * 状态：0-待确认，1-已确认，2-已取消，3-已完成
     */
    @TableField("status")
    private Integer status;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;
}
