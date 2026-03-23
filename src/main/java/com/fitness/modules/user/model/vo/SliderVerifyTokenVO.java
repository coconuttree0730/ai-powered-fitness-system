package com.fitness.modules.user.model.vo;

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
public class SliderVerifyTokenVO {

    /**
     * 验证令牌
     */
    private String token;

    /**
     * 过期时间（秒）
     */
    private Integer expireSeconds;
}
