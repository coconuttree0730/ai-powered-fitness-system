package com.fitness.modules.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 滑块验证令牌VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "滑块验证令牌响应")
public class SliderVerifyTokenVO {

    /**
     * 验证令牌
     */
    @Schema(description = "滑块验证令牌", example = "slider-token-123")
    private String token;

    /**
     * 过期时间（秒）
     */
    @Schema(description = "令牌剩余有效时长，单位秒", example = "120")
    private Integer expireSeconds;
}
