package com.fitness.modules.equipment.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 器材报修请求DTO
 */
@Data
public class RepairDTO {

    /**
     * 器材ID
     */
    @NotNull(message = "器材ID不能为空")
    private Long equipmentId;

    /**
     * 问题描述
     */
    @NotBlank(message = "问题描述不能为空")
    private String description;

    /**
     * 问题图片URL
     */
    private String imageUrl;
}
