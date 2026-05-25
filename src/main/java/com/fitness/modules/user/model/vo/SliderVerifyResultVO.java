package com.fitness.modules.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 滑块验证结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "滑块验证结果响应")
public class SliderVerifyResultVO {

    /**
     * 是否验证通过
     */
    @Schema(description = "是否通过验证", example = "true")
    private Boolean verified;

    /**
     * 验证令牌（验证通过后返回，用于后续操作）
     */
    @Schema(description = "验证通过后返回的后续操作令牌", example = "verified-slider-token")
    private String verifyToken;

    /**
     * 提示信息
     */
    @Schema(description = "结果提示信息", example = "验证通过")
    private String message;
}
