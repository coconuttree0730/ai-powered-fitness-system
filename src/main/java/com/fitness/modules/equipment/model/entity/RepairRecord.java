package com.fitness.modules.equipment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修处理记录实体类
 * 对应 fitness_repair_record 表
 * 用于记录报修的处理过程和进度
 */
@Data
@TableName("fitness_repair_record")
public class RepairRecord {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报修ID
     */
    @TableField("repair_id")
    private Long repairId;

    /**
     * 处理人ID（管理员）
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理类型：1-状态变更, 2-处理备注, 3-进度更新
     */
    @TableField("record_type")
    private Integer recordType;

    /**
     * 处理前状态
     */
    @TableField("before_status")
    private Integer beforeStatus;

    /**
     * 处理后状态
     */
    @TableField("after_status")
    private Integer afterStatus;

    /**
     * 处理内容/备注
     */
    @TableField("content")
    private String content;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
