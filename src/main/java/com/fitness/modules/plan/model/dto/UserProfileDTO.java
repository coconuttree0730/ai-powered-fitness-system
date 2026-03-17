package com.fitness.modules.plan.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户个人信息DTO
 */
@Data
public class UserProfileDTO {

    /**
     * 身高(cm)
     */
    @NotNull(message = "身高不能为空")
    @DecimalMin(value = "50", message = "身高不能小于50cm")
    @DecimalMax(value = "250", message = "身高不能大于250cm")
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "20", message = "体重不能小于20kg")
    @DecimalMax(value = "300", message = "体重不能大于300kg")
    private BigDecimal weight;

    /**
     * 年龄
     */
    @NotNull(message = "年龄不能为空")
    @Min(value = 10, message = "年龄不能小于10岁")
    @Max(value = 100, message = "年龄不能大于100岁")
    private Integer age;

    /**
     * 健身经验
     */
    @NotBlank(message = "健身经验不能为空")
    private String experience;

    /**
     * 健身目标
     */
    @NotBlank(message = "健身目标不能为空")
    private String fitnessGoal;
}
