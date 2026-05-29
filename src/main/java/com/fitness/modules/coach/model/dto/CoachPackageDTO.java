package com.fitness.modules.coach.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 教练套餐创建/编辑请求DTO
 */
@Data
public class CoachPackageDTO {

    /** 套餐名称（必填） */
    @NotBlank(message = "套餐名称不能为空")
    private String name;

    /** 套餐编码 */
    private String packageCode;

    /** 套餐描述 */
    private String description;

    /** 封面图片 */
    private String coverImage;

    /** 原价（必填） */
    @NotNull(message = "原价不能为空")
    private BigDecimal originalPrice;

    /** 总课时数 */
    private Integer totalSessions;

    /** 有效期（天） */
    private Integer validityDays;

    /** 状态 */
    private String status;

    /** 排序 */
    private Integer sortOrder;
}