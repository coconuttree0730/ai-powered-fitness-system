package com.fitness.modules.course.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 课程实体类
 * 对应 fitness_course 表
 * 业务场景：公开课是周期性课程（每周固定某天某时段）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fitness_course")
public class Course extends BaseEntity {

    /**
     * 课程ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 课程描述
     */
    @TableField("description")
    private String description;

    /**
     * 教练ID
     */
    @TableField("coach_id")
    private Long coachId;

    /**
     * 课程分类
     */
    @TableField("category")
    private String category;

    /**
     * 星期几：1-周一, 2-周二, 3-周三, 4-周四, 5-周五, 6-周六, 7-周日
     */
    @TableField("day_of_week")
    private Integer dayOfWeek;

    /**
     * 开始时间（时分秒，如 14:00:00）
     */
    @TableField("start_time")
    private LocalTime startTime;

    /**
     * 结束时间（时分秒，如 15:30:00）
     */
    @TableField("end_time")
    private LocalTime endTime;

    /**
     * 容量（可预约人数）
     */
    @TableField("capacity")
    private Integer capacity;

    /**
     * 状态：0-未开始, 1-进行中, 2-已结束, 3-已取消
     */
    @TableField("status")
    private Integer status;

    /**
     * 课程图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 难度等级：入门/初级/中级/高级/进阶
     */
    @TableField("difficulty_level")
    private String difficultyLevel;

    /**
     * 课程时长（分钟）
     */
    @TableField("duration_minutes")
    private Integer durationMinutes;

    /**
     * 最小卡路里消耗
     */
    @TableField("calories_min")
    private Integer caloriesMin;

    /**
     * 最大卡路里消耗
     */
    @TableField("calories_max")
    private Integer caloriesMax;

    /**
     * 总预约人数（统计所有预约过该课程的独立会员数量）
     */
    @TableField("total_booking_count")
    private Integer totalBookingCount;

    }
