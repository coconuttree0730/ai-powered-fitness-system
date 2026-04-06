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

    /**
     * 逻辑删除标志
     */
    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
