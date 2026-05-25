package com.fitness.modules.course.model.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 课程查询条件DTO
 */
@Data
public class CourseQueryDTO {

    /**
     * 课程分类
     */
    private String category;

    /**
     * 难度等级
     */
    private String level;

    /**
     * 排序方式
     */
    private String sortBy;

    /**
     * 星期几筛选：1-周一, 2-周二, ..., 7-周日
     */
    private Integer dayOfWeek;

    /**
     * 关键词（课程名称）
     */
    private String keyword;

    /**
     * 上课日期起始值
     */
    private LocalDate startDate;

    /**
     * 上课日期结束值
     */
    private LocalDate endDate;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 最小时长（分钟）
     */
    private Integer minDuration;

    /**
     * 最大时长（分钟）
     */
    private Integer maxDuration;

    /**
     * 最小卡路里
     */
    private Integer minCalories;

    /**
     * 最大卡路里
     */
    private Integer maxCalories;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
