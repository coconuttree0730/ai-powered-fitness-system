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
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 关键词（课程名称）
     */
    private String keyword;

    /**
     * 课程名称
     */
    private String courseName;

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
