package com.fitness.modules.equipment.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的报修记录VO
 */
@Data
public class MyRepairVO {

    /**
     * 报修ID
     */
    private Long id;

    /**
     * 器材ID
     */
    private Long equipmentId;

    /**
     * 器材名称
     */
    private String equipmentName;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 问题图片URL
     */
    private String imageUrl;

    /**
     * 状态：0-待处理, 1-处理中, 2-已完成
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 处理完成时间
     */
    private LocalDateTime handleTime;
}
