package com.fitness.modules.equipment.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修处理记录展示VO
 */
@Data
public class RepairRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 报修ID
     */
    private Long repairId;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人名称
     */
    private String handlerName;

    /**
     * 处理类型：1-状态变更, 2-处理备注, 3-进度更新
     */
    private Integer recordType;

    /**
     * 处理前状态
     */
    private Integer beforeStatus;

    /**
     * 处理后状态
     */
    private Integer afterStatus;

    /**
     * 处理内容/备注
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
