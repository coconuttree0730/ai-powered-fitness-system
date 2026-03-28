package com.fitness.modules.user.service;

/**
 * 邮箱验证码服务接口
 */
public interface EmailCodeService {

    /**
     * 生成并发送邮箱验证码
     *
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    boolean sendEmailCode(String email);

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱地址
     * @param code  验证码
     * @return 是否验证通过
     */
    boolean verifyEmailCode(String email, String code);

    /**
     * 检查是否可以发送验证码（防频繁）
     *
     * @param email 邮箱地址
     * @return 是否可以发送
     */
    boolean canSend(String email);

    /**
     * 获取剩余冷却时间（秒）
     *
     * @param email 邮箱地址
     * @return 剩余秒数，0表示可以发送
     */
    long getRemainingCooldown(String email);
}
