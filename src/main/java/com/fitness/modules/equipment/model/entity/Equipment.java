package com.fitness.modules.equipment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 器材实体类
 * 对应 fitness_equipment 表
 */
@Data
@TableName("fitness_equipment")
public class Equipment {

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
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
