package com.fitness.modules.user.service.impl;

import com.fitness.modules.user.service.EmailCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

    private final StringRedisTemplate redisTemplate;

    // Redis键前缀
    private static final String EMAIL_CODE_KEY_PREFIX = "email:code:";
    private static final String EMAIL_CODE_COOLDOWN_KEY_PREFIX = "email:cooldown:";

    // 验证码过期时间（5分钟）
    private static final long CODE_EXPIRE_MINUTES = 5;
    // 发送冷却时间（60秒）
    private static final long COOLDOWN_SECONDS = 60;
    // 验证码长度
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    @Override
    public boolean sendEmailCode(String email) {
        // 1. 检查冷却时间
        if (!canSend(email)) {
            log.warn("邮箱验证码发送过于频繁: email={}", email);
            return false;
        }

        // 2. 生成6位数字验证码
        String code = generateCode();

        // 3. 存储验证码到Redis
        String codeKey = EMAIL_CODE_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 4. 设置冷却时间
        String cooldownKey = EMAIL_CODE_COOLDOWN_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        // 5. 模拟发送邮件（后续接入真实邮件服务）
        // TODO 模拟发送邮件: 后续接入 邮件服务进行邮件发送
        log.info("邮箱验证码发送成功: email={}", email);

        return true;
    }

    @Override
    public boolean verifyEmailCode(String email, String code) {
        String codeKey = EMAIL_CODE_KEY_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            log.warn("邮箱验证码验证失败: 验证码已过期或不存在, email={}", email);
            return false;
        }

        boolean matched = storedCode.equals(code);
        if (matched) {
            // 验证通过后删除验证码（一次性使用）
            redisTemplate.delete(codeKey);
            log.info("邮箱验证码验证成功: email={}", email);
        } else {
            log.warn("邮箱验证码验证失败: 验证码不匹配, email={}", email);
        }

        return matched;
    }

    @Override
    public boolean canSend(String email) {
        String cooldownKey = EMAIL_CODE_COOLDOWN_KEY_PREFIX + email;
        Boolean exists = redisTemplate.hasKey(cooldownKey);
        return !Boolean.TRUE.equals(exists);
    }

    @Override
    public long getRemainingCooldown(String email) {
        String cooldownKey = EMAIL_CODE_COOLDOWN_KEY_PREFIX + email;
        Long expire = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        return expire != null && expire > 0 ? expire : 0;
    }

    /**
     * 生成6位数字验证码
     *
     * @return 验证码
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }
        return code.toString();
    }
}
