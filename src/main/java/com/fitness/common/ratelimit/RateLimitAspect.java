package com.fitness.common.ratelimit;

import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.integration.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;

/**
 * 接口限流切面 — Redis Lua 滑动窗口算法
 *
 * Lua 脚本逻辑：
 * 1. ZREMRANGEBYSCORE 移除窗口外的旧请求记录
 * 2. ZCARD 统计窗口内请求数
 * 3. 如果未超限，ZADD 添加当前请求并设置 EXPIRE
 * 4. 返回当前窗口内请求数
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    private static final DefaultRedisScript<Long> SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>(
            """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])

            -- 移除窗口外的记录
            redis.call('ZREMRANGEBYSCORE', key, 0, now - window * 1000)

            -- 统计窗口内请求数
            local count = redis.call('ZCARD', key)

            if count < limit then
                -- 未超限，添加请求记录
                redis.call('ZADD', key, now, now .. ':' .. math.random(100000))
                redis.call('EXPIRE', key, window)
                return count + 1
            end

            return -1
            """,
            Long.class
    );

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);

        Long result = stringRedisTemplate.execute(
                SLIDING_WINDOW_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(rateLimit.limit()),
                String.valueOf(rateLimit.window()),
                String.valueOf(System.currentTimeMillis())
        );

        if (result != null && result == -1L) {
            log.warn("[RATE_LIMIT] key={}, limit={}/{}, 被限流",
                    key, rateLimit.limit(), rateLimit.window());
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS,
                    "请求过于频繁，请" + rateLimit.window() + "秒后再试");
        }

        return joinPoint.proceed();
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder("rate-limit:");
        keyBuilder.append(rateLimit.key());

        switch (rateLimit.dimension()) {
            case IP -> {
                String ip = getClientIp();
                keyBuilder.append(":ip:").append(ip);
            }
            case USER -> {
                Long userId = SecurityUtils.getCurrentUserId();
                keyBuilder.append(":user:").append(userId != null ? userId : "anonymous");
            }
            case CUSTOM -> {
                // 自定义维度：从 SpEL 表达式解析
                String spelKey = resolveSpelKey(joinPoint, rateLimit.key());
                keyBuilder = new StringBuilder("rate-limit:").append(spelKey);
            }
        }

        return keyBuilder.toString();
    }

    private String resolveSpelKey(ProceedingJoinPoint joinPoint, String spelExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }

        // 支持 #paramName 格式的 SpEL
        String expression = spelExpression.startsWith("#") ? spelExpression : "#" + spelExpression;
        try {
            Object value = PARSER.parseExpression(expression).getValue(context);
            return value != null ? value.toString() : "unknown";
        } catch (Exception e) {
            log.warn("SpEL 解析失败: expression={}, error={}", spelExpression, e.getMessage());
            return spelExpression;
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "unknown";
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0].trim();
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
