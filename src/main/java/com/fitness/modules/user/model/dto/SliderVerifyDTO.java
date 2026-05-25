package com.fitness.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 滑块验证请求DTO
 */
@Data
@Schema(description = "滑块验证结果提交请求")
public class SliderVerifyDTO {

    /**
     * 验证令牌
     */
    @NotBlank(message = "验证令牌不能为空")
    private String token;

    /**
     * 滑块值（偏移量）
     */
    @NotNull(message = "滑块值不能为空")
    private Integer sliderValue;

    /**
     * 时间戳
     */
    @NotNull(message = "时间戳不能为空")
    private Long timestamp;
}
