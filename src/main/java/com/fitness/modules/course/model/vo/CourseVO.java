package com.fitness.modules.course.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程信息展示VO
 */
@Data
public class CourseVO {

    /**
     * 课程ID
     */
    private Long id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 教练姓名
     */
    private String coachName;

    /**
     * 教练头像
     */
    private String coachAvatar;

    /**
     * 课程分类
     */
    private String category;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 容量（可预约人数）
     */
    private Integer capacity;

    /**
     * 已预约数
     */
    private Integer bookedCount;

    /**
     * 剩余可预约数
     */
    private Integer remainingCount;

    /**
     * 状态：0-未开始, 1-进行中, 2-已结束, 3-已取消
     */
    private Integer status;

    /**
     * 课程图片URL
     */
    private String imageUrl;

    /**
     * 难度等级：入门/初级/中级/高级/进阶
     */
    private String difficultyLevel;

    /**
     * 课程时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 最小卡路里消耗
     */
    private Integer caloriesMin;

    /**
     * 最大卡路里消耗
     */
    private Integer caloriesMax;
}
