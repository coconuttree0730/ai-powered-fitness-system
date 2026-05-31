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

    @NotBlank(message = "套餐名称不能为空")
    private String name;

    private String packageCode;

    private String description;

    private String coverImage;

    @NotNull(message = "原价不能为空")
    private BigDecimal originalPrice;

    private Integer totalSessions;

    private Integer validityDays;

    private String status;

    private Integer sortOrder;

    private Long coachId;
}