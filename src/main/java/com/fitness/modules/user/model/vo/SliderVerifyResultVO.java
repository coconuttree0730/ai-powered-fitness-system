package com.fitness.modules.user.model.vo;

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
public class SliderVerifyResultVO {

    /**
     * 是否验证通过
     */
    private Boolean verified;

    /**
     * 验证令牌（验证通过后返回，用于后续操作）
     */
    private String verifyToken;

    /**
     * 提示信息
     */
    private String message;
}
