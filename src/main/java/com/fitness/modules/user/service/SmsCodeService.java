package com.fitness.modules.user.service;

/**
 * 短信验证码服务接口
 */
public interface SmsCodeService {

    /**
     * 生成并发送短信验证码
     *
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendSmsCode(String phone);

    /**
     * 验证短信验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否验证通过
     */
    boolean verifySmsCode(String phone, String code);

    /**
     * 检查是否可以发送验证码（防频繁）
     *
     * @param phone 手机号
     * @return 是否可以发送
     */
    boolean canSend(String phone);

    /**
     * 获取剩余冷却时间（秒）
     *
     * @param phone 手机号
     * @return 剩余秒数，0表示可以发送
     */
    long getRemainingCooldown(String phone);

    /**
     * 检查是否超过每日发送限制
     *
     * @param phone 手机号
     * @return 是否超过限制
     */
    boolean isDailyLimitExceeded(String phone);

    /**
     * 获取今日剩余发送次数
     *
     * @param phone 手机号
     * @return 剩余次数
     */
    int getRemainingDailyCount(String phone);
}
