package com.fitness.modules.plan.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健身计划VO
 */
@Data
public class PlanVO {

    /**
     * 计划ID
     */
    private Long id;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 健身目标
     */
    private String goal;

    /**
     * 计划周期(天)
     */
    private Integer duration;

    /**
     * 状态: 0-已停用, 1-进行中
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 计划详情列表
     */
    private List<PlanDetailVO> details;
}
