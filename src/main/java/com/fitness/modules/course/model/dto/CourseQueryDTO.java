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
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 关键词（课程名称）
     */
    private String keyword;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
