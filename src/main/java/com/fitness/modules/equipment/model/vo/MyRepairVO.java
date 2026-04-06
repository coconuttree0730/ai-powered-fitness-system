package com.fitness.modules.equipment.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
     * 问题描述
     */
    private String description;

    /**
     * 问题图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 状态：0-待处理, 1-处理中, 2-已完成, 3-已关闭
     */
    private Integer status;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 处理完成时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理记录列表
     */
    private List<RepairRecordVO> records;
}
