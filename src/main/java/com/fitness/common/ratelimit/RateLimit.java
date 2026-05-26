package com.fitness.common.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解 — 基于 Redis Lua 滑动窗口算法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流 key 前缀（支持 SpEL，如 "sms:#phone"） */
    String key();

    /** 窗口内最大请求数 */
    int limit() default 10;

    /** 窗口大小（秒） */
    int window() default 60;

    /** 限流维度：IP / USER / CUSTOM */
    RateLimitDimension dimension() default RateLimitDimension.IP;
}
