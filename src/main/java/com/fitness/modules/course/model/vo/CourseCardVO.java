package com.fitness.modules.course.model.vo;

import lombok.Data;

/**
 * 课程卡片VO（用于首页课程体系展示）
 */
@Data
public class CourseCardVO {

    /**
     * 课程ID
     */
    private Long id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 课程分类key
     */
    private String category;

    /**
     * 难度等级：入门/初级/中级/高级/进阶
     */
    private String level;

    /**
     * 课程时长（分钟）
     */
    private Integer duration;

    /**
     * 卡路里消耗范围（如：500-700卡）
     */
    private String calories;

    /**
     * 已预约人数
     */
    private Integer bookings;

    /**
     * 课程图片URL
     */
    private String image;

    /**
     * 课程描述
     */
    private String desc;
}
