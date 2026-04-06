package com.fitness.modules.equipment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 器材类型实体类
 * 对应 fitness_equipment_type 表
 */
@Data
@TableName("fitness_equipment_type")
public class EquipmentType {

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

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
