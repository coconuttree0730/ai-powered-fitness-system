package com.fitness.modules.plan.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生成健身计划请求DTO
 */
@Data
public class PlanGenerateDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 计划名称
     */
    @NotBlank(message = "计划名称不能为空")
    private String planName;

    /**
     * 健身目标
     */
    @NotBlank(message = "健身目标不能为空")
    private String goal;

    /**
     * 训练部位
     */
    private String bodyPart;

    /**
     * 经验水平
     */
    @NotBlank(message = "经验水平不能为空")
    private String level;

    /**
     * 经验水平（兼容字段）
     */
    private String experience;

    /**
     * 计划周期（周数）
     */
    private Integer duration = 4;
}
