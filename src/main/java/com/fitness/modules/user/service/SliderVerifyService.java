package com.fitness.modules.user.service;

import com.fitness.modules.user.model.vo.SliderVerifyResultVO;
import com.fitness.modules.user.model.vo.SliderVerifyTokenVO;

/**
 * 滑块验证服务接口
 */
public interface SliderVerifyService {

    /**
     * 生成滑块验证令牌
     *
     * @return 验证令牌VO
     */
    SliderVerifyTokenVO generateToken();

    /**
     * 验证滑块结果
     *
     * @param token       验证令牌
     * @param sliderValue 滑块值
     * @param timestamp   时间戳
     * @return 验证结果
     */
    SliderVerifyResultVO verify(String token, Integer sliderValue, Long timestamp);

    /**
     * 检查验证令牌是否已验证通过
     *
     * @param token 验证令牌
     * @return 是否已验证
     */
    boolean isVerified(String token);

    /**
     * 使验证令牌失效
     *
     * @param token 验证令牌
     */
    void invalidateToken(String token);
}
