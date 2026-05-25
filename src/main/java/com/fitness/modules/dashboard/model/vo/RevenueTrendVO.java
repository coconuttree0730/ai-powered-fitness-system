package com.fitness.modules.dashboard.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 营收趋势数据VO
 */
@Data
public class RevenueTrendVO {

    /**
     * 日期
     */
    private String date;

    /**
     * 营收金额
     */
    private BigDecimal amount;
}
