package com.fitness.modules.plan.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成健身计划请求DTO
 */
@Data
public class PlanGenerateDTO {

    /**
     * 健身目标
     */
    @NotBlank(message = "健身目标不能为空")
    private String goal;

    /**
     * 训练部位
     */
    @NotBlank(message = "训练部位不能为空")
    private String bodyPart;

    /**
     * 经验水平
     */
    @NotBlank(message = "经验水平不能为空")
    private String experience;
}
