package com.fitness.modules.dashboard.model.vo;

import lombok.Data;

/**
 * 报修处理统计VO
 */
@Data
public class RepairStatsVO {

    /**
     * 状态名称
     */
    private String status;

    /**
     * 数量
     */
    private Integer count;
}
