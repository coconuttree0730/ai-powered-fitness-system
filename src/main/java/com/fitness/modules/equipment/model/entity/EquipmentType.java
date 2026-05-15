package com.fitness.modules.equipment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 器材类型实体类
 * 对应 fitness_equipment_type 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fitness_equipment_type")
public class EquipmentType extends BaseEntity {

    /**
     * 类型ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 类型编码
     */
    @TableField("type_code")
    private String typeCode;

    /**
     * 类型名称
     */
    @TableField("type_name")
    private String typeName;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 描述
     */
    @TableField("description")
    private String description;
}
