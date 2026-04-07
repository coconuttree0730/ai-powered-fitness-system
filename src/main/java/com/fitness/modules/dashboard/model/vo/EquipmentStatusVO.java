package com.fitness.modules.dashboard.model.vo;

import lombok.Data;

/**
 * 器材使用状态统计VO
 */
@Data
public class EquipmentStatusVO {

    /**
     * 正常使用的器材数量
     */
    private Integer normal;

    /**
     * 维护中的器材数量
     */
    private Integer maintenance;

    /**
     * 待维修的器材数量
     */
    private Integer repair;

    /**
     * 已停用/报废的器材数量
     */
    private Integer offline;
}
