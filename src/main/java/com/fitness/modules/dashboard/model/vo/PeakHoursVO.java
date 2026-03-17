package com.fitness.modules.dashboard.model.vo;

import lombok.Data;

/**
 * 到店高峰时间统计VO
 */
@Data
public class PeakHoursVO {

    /**
     * 小时
     */
    private Integer hour;

    /**
     * 人数
     */
    private Integer count;
}
