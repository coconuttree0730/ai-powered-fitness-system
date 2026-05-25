package com.fitness.modules.user.service.impl;

import com.fitness.modules.user.model.vo.SliderVerifyResultVO;
import com.fitness.modules.user.model.vo.SliderVerifyTokenVO;
import com.fitness.modules.user.service.SliderVerifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 滑块验证服务实现类
 * 方案B：简化验证，只验证滑动是否到位（滑动距离 > 90%）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SliderVerifyServiceImpl implements SliderVerifyService {

    private final StringRedisTemplate redisTemplate;

    // Redis键前缀
    private static final String SLIDER_VERIFY_KEY_PREFIX = "slider:verify:";
    private static final String SLIDER_VERIFY_PASSED_KEY_PREFIX = "slider:verify:passed:";

    // 验证令牌过期时间（5分钟）
    private static final long TOKEN_EXPIRE_MINUTES = 5;
    // 验证通过后令牌过期时间（10分钟）
    private static final long VERIFIED_TOKEN_EXPIRE_MINUTES = 10;
    // 滑块轨道总宽度（像素）
    private static final int SLIDER_TRACK_WIDTH = 320;
    // 滑块按钮宽度（像素）
    private static final int SLIDER_BTN_WIDTH = 40;
    // 成功阈值（滑动到90%以上算成功）
    private static final double SUCCESS_THRESHOLD = 0.90;

    @Override
    public SliderVerifyTokenVO generateToken() {
        // 生成唯一令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        String redisKey = SLIDER_VERIFY_KEY_PREFIX + token;

        // 存储令牌到Redis（值为"pending"表示待验证）
        redisTemplate.opsForValue().set(redisKey, "pending",
                TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("生成滑块验证令牌: {}", token);

        return SliderVerifyTokenVO.builder()
                .token(token)
                .expireSeconds((int) (TOKEN_EXPIRE_MINUTES * 60))
                .build();
    }

    @Override
    public SliderVerifyResultVO verify(String token, Integer sliderValue, Long timestamp) {
        String redisKey = SLIDER_VERIFY_KEY_PREFIX + token;

        // 1. 检查令牌是否存在
        String tokenStatus = redisTemplate.opsForValue().get(redisKey);
        if (tokenStatus == null) {
            log.warn("滑块验证失败: 令牌不存在或已过期, token={}", token);
            return SliderVerifyResultVO.builder()
                    .verified(false)
                    .message("验证已过期，请重新获取")
                    .build();
        }

        // 2. 检查时间戳（防止重放攻击，5分钟内有效）
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;
        if (timeDiff < 0 || timeDiff > 5 * 60 * 1000) {
            log.warn("滑块验证失败: 时间戳异常, token={}, timeDiff={}ms", token, timeDiff);
            return SliderVerifyResultVO.builder()
                    .verified(false)
                    .message("验证超时，请重新尝试")
                    .build();
        }

        // 3. 验证滑块值（方案B：验证是否滑动到位，>90%算成功）
        int maxSliderValue = SLIDER_TRACK_WIDTH - SLIDER_BTN_WIDTH; // 280
        double progress = (double) sliderValue / maxSliderValue;

        if (progress < SUCCESS_THRESHOLD) {
            log.warn("滑块验证失败: 滑动未到位, token={}, sliderValue={}, progress={}%",
                    token, sliderValue, String.format("%.2f", progress * 100));
            return SliderVerifyResultVO.builder()
                    .verified(false)
                    .message("验证未通过，请拖动滑块到最右侧")
                    .build();
        }

        // 4. 验证通过，删除原令牌，存储验证通过标记
        redisTemplate.delete(redisKey);
        String passedKey = SLIDER_VERIFY_PASSED_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(passedKey, "1", VERIFIED_TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("滑块验证成功: token={}, sliderValue={}, progress={}%",
                token, sliderValue, String.format("%.2f", progress * 100));

        return SliderVerifyResultVO.builder()
                .verified(true)
                .verifyToken(token)
                .message("验证通过")
                .build();
    }

    @Override
    public boolean isVerified(String token) {
        String passedKey = SLIDER_VERIFY_PASSED_KEY_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(passedKey);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void invalidateToken(String token) {
        String redisKey = SLIDER_VERIFY_KEY_PREFIX + token;
        String passedKey = SLIDER_VERIFY_PASSED_KEY_PREFIX + token;
        redisTemplate.delete(redisKey);
        redisTemplate.delete(passedKey);
        log.info("滑块验证令牌已失效: {}", token);
    }
}
