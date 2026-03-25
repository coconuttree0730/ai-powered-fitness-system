package com.fitness.modules.user.service.impl;

import com.fitness.modules.user.service.SmsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeServiceImpl implements SmsCodeService {

    private final StringRedisTemplate redisTemplate;

    // Redis键前缀
    private static final String SMS_CODE_KEY_PREFIX = "sms:code:";
    private static final String SMS_CODE_COOLDOWN_KEY_PREFIX = "sms:cooldown:";

    // 验证码过期时间（5分钟）
    private static final long CODE_EXPIRE_MINUTES = 5;
    // 发送冷却时间（60秒）
    private static final long COOLDOWN_SECONDS = 60;
    // 验证码长度
    private static final int CODE_LENGTH = 6;

    @Override
    public boolean sendSmsCode(String phone) {
        // 1. 检查冷却时间
        if (!canSend(phone)) {
            log.warn("短信验证码发送过于频繁: phone={}", phone);
            return false;
        }

        // 2. 生成6位数字验证码
        String code = generateCode();

        // 3. 存储验证码到Redis
        String codeKey = SMS_CODE_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 4. 设置冷却时间
        String cooldownKey = SMS_CODE_COOLDOWN_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        // 5. 模拟发送短信（后续接入真实短信服务）
        log.info("短信验证码发送成功: phone={}, code={}", phone, code);

        return true;
    }

    @Override
    public boolean verifySmsCode(String phone, String code) {
        String codeKey = SMS_CODE_KEY_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            log.warn("短信验证码验证失败: 验证码已过期或不存在, phone={}", phone);
            return false;
        }

        boolean matched = storedCode.equals(code);
        if (matched) {
            // 验证通过后删除验证码（一次性使用）
            redisTemplate.delete(codeKey);
            log.info("短信验证码验证成功: phone={}", phone);
        } else {
            log.warn("短信验证码验证失败: 验证码不匹配, phone={}", phone);
        }

        return matched;
    }

    @Override
    public boolean canSend(String phone) {
        String cooldownKey = SMS_CODE_COOLDOWN_KEY_PREFIX + phone;
        Boolean exists = redisTemplate.hasKey(cooldownKey);
        return !Boolean.TRUE.equals(exists);
    }

    @Override
    public long getRemainingCooldown(String phone) {
        String cooldownKey = SMS_CODE_COOLDOWN_KEY_PREFIX + phone;
        Long expire = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        return expire != null && expire > 0 ? expire : 0;
    }

    /**
     * 生成6位数字验证码
     *
     * @return 验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
