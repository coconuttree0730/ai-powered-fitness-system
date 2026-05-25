package com.fitness.modules.chat.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程推荐VO
 * 根据健身计划推荐的课程
 */
@Data
public class CourseRecommendVO {

    /**
     * 课程ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 教练姓名
     */
    private String coachName;

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
     * 课程图片URL
     */
    private String imageUrl;

    /**
     * 难度等级
     */
    private String difficultyLevel;

    /**
     * 课程时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 剩余可预约数
     */
    private Integer remainingCount;

    /**
     * 推荐理由
     */
    private String recommendReason;
}
