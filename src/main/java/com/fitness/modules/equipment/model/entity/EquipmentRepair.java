package com.fitness.modules.equipment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 器材报修实体类
 * 对应 fitness_equipment_repair 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fitness_equipment_repair")
public class EquipmentRepair extends BaseEntity {

    /**
     * 报修ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报修用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 关联的器材ID
     */
    @TableField("equipment_id")
    private Long equipmentId;

    /**
     * 报修描述
     */
    @TableField("description")
    private String description;

    /**
     * 报修图片URL（多张图片以逗号分隔）
     */
    @TableField("image_urls")
    private String imageUrls;

    /**
     * 状态：0-待处理, 1-处理中, 2-已完成, 3-已关闭
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    private String handleRemark;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;
}
