package com.fitness.modules.equipment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 器材实体类
 * 对应 fitness_equipment 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fitness_equipment")
public class Equipment extends BaseEntity {

    /**
     * 器材ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 器材名称
     */
    @TableField("equipment_name")
    private String equipmentName;

    /**
     * 器材位置
     */
    @TableField("location")
    private String location;

    /**
     * 状态：0-维修中, 1-正常, 2-已报废
     */
    @TableField("status")
    private Integer status;

    /**
     * 器材描述
     */
    @TableField("description")
    private String description;

    /**
     * 器材图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 购买日期
     */
    @TableField("purchase_date")
    private LocalDate purchaseDate;

    /**
     * 器材类型编码
     */
    @TableField("type_code")
    private String typeCode;

    /**
     * 器材编号
     */
    @TableField("equipment_no")
    private String equipmentNo;
}
