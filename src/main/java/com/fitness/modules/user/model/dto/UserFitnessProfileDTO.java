package com.fitness.modules.user.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户健身档案DTO
 */
@Data
public class UserFitnessProfileDTO {

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
    @Digits(integer = 3, fraction = 1, message = "体重格式不正确")
    private BigDecimal weight;

    /**
     * 年龄
     */
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄不能小于1岁")
    @Max(value = 150, message = "年龄不能大于150岁")
    private Integer age;

    /**
     * 健身经验：BEGINNER-初学者, INTERMEDIATE-中级, ADVANCED-高级
     */
    @NotBlank(message = "健身经验不能为空")
    @Pattern(regexp = "^(BEGINNER|INTERMEDIATE|ADVANCED)$", message = "健身经验必须是 BEGINNER、INTERMEDIATE 或 ADVANCED")
    private String experience;

    /**
     * 健身目标：WEIGHT_LOSS-减脂, MUSCLE_GAIN-增肌, BODY_SHAPING-塑形, ENDURANCE-增强体能, HEALTH-保持健康
     */
    @NotBlank(message = "健身目标不能为空")
    @Pattern(regexp = "^(WEIGHT_LOSS|MUSCLE_GAIN|BODY_SHAPING|ENDURANCE|HEALTH)$", message = "健身目标格式不正确")
    private String fitnessGoal;

    /**
     * 性别：MALE-男, FEMALE-女
     */
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "性别必须是 MALE 或 FEMALE")
    private String gender;
}
