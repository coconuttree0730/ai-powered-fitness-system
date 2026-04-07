package com.fitness.modules.dashboard.model.vo;

import lombok.Data;

/**
 * 用户增长趋势VO
 */
@Data
public class UserGrowthVO {

    /**
     * 日期
     */
    private String date;

    /**
     * 新增用户数量
     */
    private Integer count;
}
