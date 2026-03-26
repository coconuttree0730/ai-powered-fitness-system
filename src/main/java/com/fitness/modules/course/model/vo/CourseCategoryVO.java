package com.fitness.modules.course.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 课程体系分类VO（用于首页展示）
 */
@Data
public class CourseCategoryVO {

    /**
     * 分类key
     */
    private String key;

    /**
     * 分类标签
     */
    private String label;

    /**
     * 该分类下的课程列表
     */
    private List<CourseCardVO> courses;
}
