package com.fitness.modules.dashboard.model.vo;

import lombok.Data;

/**
 * 课程统计VO
 */
@Data
public class CourseStatsVO {

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 课程数量
     */
    private Integer courseCount;

    /**
     * 预约数量
     */
    private Integer bookingCount;
}
