package com.fitness.modules.equipment.model.dto;

import lombok.Data;

/**
 * 器材查询请求DTO
 */
@Data
public class EquipmentQueryDTO {

    /**
     * 关键字（器材名称）
     */
    private String keyword;

    /**
     * 状态：0-维修中, 1-正常, 2-已报废
     */
    private Integer status;

    /**
     * 器材类型编码
     */
    private String typeCode;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
