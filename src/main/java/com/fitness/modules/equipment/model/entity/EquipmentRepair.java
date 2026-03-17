package com.fitness.modules.equipment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 器材报修实体类
 * 对应 fitness_equipment_repair 表
 */
@Data
@TableName("fitness_equipment_repair")
public class EquipmentRepair {

    /**
     * 报修ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 器材ID
     */
    @TableField("equipment_id")
    private Long equipmentId;

    /**
     * 报修用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 报修描述
     */
    @TableField("description")
    private String description;

    /**
     * 报修图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 状态：0-待处理, 1-处理中, 2-已完成, 3-已关闭
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
