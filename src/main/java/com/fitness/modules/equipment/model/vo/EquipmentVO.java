package com.fitness.modules.equipment.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 器材信息展示VO
 */
@Data
public class EquipmentVO {

    /**
     * 器材ID
     */
    private Long id;

    /**
     * 器材名称
     */
    private String equipmentName;

    /**
     * 器材位置
     */
    private String location;

    /**
     * 状态：0-维修中, 1-正常, 2-已报废
     */
    private Integer status;

    /**
     * 器材描述
     */
    private String description;

    /**
     * 器材图片URL
     */
    private String imageUrl;

    /**
     * 购买日期
     */
    private LocalDate purchaseDate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
