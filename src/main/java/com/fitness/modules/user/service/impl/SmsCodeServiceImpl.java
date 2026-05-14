package com.fitness.modules.user.service.impl;

import com.fitness.common.exception.BusinessException;
import com.fitness.integration.sms.exception.SmsIntegrationException;
import com.fitness.integration.sms.model.SmsSendResult;
import com.fitness.integration.sms.service.SmsSender;
import com.fitness.modules.user.service.SmsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeServiceImpl implements SmsCodeService {

    private static final String SMS_CODE_KEY_PREFIX = "sms:code:";
    private static final String SMS_CODE_COOLDOWN_KEY_PREFIX = "sms:cooldown:";
    private static final String SMS_CODE_DAILY_COUNT_KEY_PREFIX = "sms:daily:count:";
    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final long COOLDOWN_SECONDS = 60;
    private static final int CODE_LENGTH = 6;
    private static final int DAILY_LIMIT = 5;

    private final StringRedisTemplate redisTemplate;
    private final SmsSender smsSender;

    @Override
    public boolean sendSmsCode(String phone) {
        if (!canSend(phone)) {
            log.warn("短信验证码发送过于频繁: phone={}", phone);
            return false;
        }

        if (isDailyLimitExceeded(phone)) {
            log.warn("短信验证码发送超过每日限制: phone={}", phone);
            throw new BusinessException("今日发送次数已达上限，请明天再试");
        }

        String code = generateCode();
        String codeKey = SMS_CODE_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        String cooldownKey = SMS_CODE_COOLDOWN_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        incrementDailyCount(phone);

        try {
            SmsSendResult result = smsSender.sendVerifyCode(phone, code, (int) CODE_EXPIRE_MINUTES);
            if (result.success()) {
                log.info("短信验证码发送成功: phone={}", phone);
                return true;
            }
            log.error("短信验证码发送失败: phone={}, code={}, message={}", phone, result.code(), result.message());
        } catch (SmsIntegrationException e) {
            log.error("短信集成调用异常: phone={}, message={}", phone, e.getMessage(), e);
        }

        redisTemplate.delete(codeKey);
        redisTemplate.delete(cooldownKey);
        throw new BusinessException("短信发送失败，请检查手机号是否正确");
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

    @Override
    public boolean isDailyLimitExceeded(String phone) {
        String dailyCountKey = SMS_CODE_DAILY_COUNT_KEY_PREFIX + phone;
        String countStr = redisTemplate.opsForValue().get(dailyCountKey);
        if (countStr == null) {
            return false;
        }
        int count = Integer.parseInt(countStr);
        return count >= DAILY_LIMIT;
    }

    @Override
    public int getRemainingDailyCount(String phone) {
        String dailyCountKey = SMS_CODE_DAILY_COUNT_KEY_PREFIX + phone;
        String countStr = redisTemplate.opsForValue().get(dailyCountKey);
        if (countStr == null) {
            return DAILY_LIMIT;
        }
        int count = Integer.parseInt(countStr);
        return Math.max(0, DAILY_LIMIT - count);
    }

    private void incrementDailyCount(String phone) {
        String dailyCountKey = SMS_CODE_DAILY_COUNT_KEY_PREFIX + phone;
        String countStr = redisTemplate.opsForValue().get(dailyCountKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        long secondsUntilMidnight = getSecondsUntilMidnight();
        redisTemplate.opsForValue().set(dailyCountKey, String.valueOf(count + 1), secondsUntilMidnight, TimeUnit.SECONDS);
    }

    private long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().atStartOfDay().plusDays(1);
        return Duration.between(now, midnight).getSeconds();
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
