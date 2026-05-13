package com.fitness.modules.user.service.impl;

import com.fitness.integration.sms.service.AliyunSmsService;
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

    private final AliyunSmsService aliyunSmsService;

    // Redis键前缀
    // 短信验证码前缀
    private static final String SMS_CODE_KEY_PREFIX = "sms:code:";
    // 短信验证码冷却时间前缀
    private static final String SMS_CODE_COOLDOWN_KEY_PREFIX = "sms:cooldown:";
    // 短信验证码每日发送次数前缀
    private static final String SMS_CODE_DAILY_COUNT_KEY_PREFIX = "sms:daily:count:";
    // 验证码过期时间（5分钟）********
    private static final long CODE_EXPIRE_MINUTES = 5;
    // 发送冷却时间（60秒）
    private static final long COOLDOWN_SECONDS = 60;
    // 验证码长度
    private static final int CODE_LENGTH = 6;
    // 每日发送限制次数
    private static final int DAILY_LIMIT = 5;

    @Override
    public boolean sendSmsCode(String phone) {
        // 1. 检查冷却时间
        if (!canSend(phone)) {
            log.warn("短信验证码发送过于频繁: phone={}", phone);
            return false;
        }

        // 2. 检查每日发送限制
        if (isDailyLimitExceeded(phone)) {
            log.warn("短信验证码发送超过每日限制: phone={}", phone);
            throw new com.fitness.common.exception.BusinessException("今日发送次数已达上限，请明天再试");
        }

        // 3. 生成6位数字验证码
        String code = generateCode();

        // 4. 存储验证码到Redis***********  redis 的key有效时长就是 短信服务的有效时长
        String codeKey = SMS_CODE_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 5. 设置冷却时间
        String cooldownKey = SMS_CODE_COOLDOWN_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        // 6. 增加每日发送次数
        incrementDailyCount(phone);

        // 7. 调用阿里云短信认证服务发送验证码
        boolean sent = aliyunSmsService.sendVerifyCode(phone, code, (int) CODE_EXPIRE_MINUTES);
        if (!sent) {
            log.error("阿里云短信验证码发送失败: phone={}", phone);
            redisTemplate.delete(SMS_CODE_KEY_PREFIX + phone);
            redisTemplate.delete(SMS_CODE_COOLDOWN_KEY_PREFIX + phone);
            throw new com.fitness.common.exception.BusinessException("短信发送失败，请检查手机号是否正确");
        }

        log.info("短信验证码发送成功: phone={}", phone);

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
        //检查冷却时间
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

    /**
     * 增加每日发送次数
     *
     * @param phone 手机号
     */
    private void incrementDailyCount(String phone) {
        String dailyCountKey = SMS_CODE_DAILY_COUNT_KEY_PREFIX + phone;
        String countStr = redisTemplate.opsForValue().get(dailyCountKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        // 计算到今天结束剩余的秒数
        long secondsUntilMidnight = getSecondsUntilMidnight();

        redisTemplate.opsForValue().set(dailyCountKey, String.valueOf(count + 1), secondsUntilMidnight, TimeUnit.SECONDS);
    }

    /**
     * 获取距离今天结束剩余的秒数
     *
     * @return 剩余秒数
     */
    private long getSecondsUntilMidnight() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime midnight = now.toLocalDate().atStartOfDay().plusDays(1);
        return java.time.Duration.between(now, midnight).getSeconds();
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
