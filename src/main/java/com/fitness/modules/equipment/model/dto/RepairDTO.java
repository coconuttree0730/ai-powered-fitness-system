package com.fitness.modules.equipment.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 器材报修请求DTO
 */
@Data
public class RepairDTO {

    /**
     * 关联的器材ID
     */
    private Long equipmentId;

    /**
     * 问题描述
     */
    @NotBlank(message = "问题描述不能为空")
    private String description;

    /**
     * 问题图片URL列表（最多5张）
     */
    @Size(max = 5, message = "图片最多上传5张")
    private List<String> imageUrls;
}
