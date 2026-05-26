package com.fitness.modules.user.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.fitness.common.config.JwtProperties;
import com.fitness.modules.user.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_BLACKLIST_PREFIX = "token:blacklist:user:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    private final Cache<Long, Long> userBlacklistCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .maximumSize(1000)
            .build();

    @Override
    public void blacklistToken(String jti, long ttlMillis) {
        try {
            String key = TOKEN_BLACKLIST_PREFIX + jti;
            redisTemplate.opsForValue().set(key, "1", ttlMillis, TimeUnit.MILLISECONDS);
            log.debug("Token已加入黑名单: jti={}, ttl={}ms", jti, ttlMillis);
        } catch (Exception e) {
            log.error("将Token加入黑名单失败: jti={}, error={}", jti, e.getMessage());
        }
    }

    @Override
    public void blacklistAllUserTokens(Long userId) {
        try {
            String key = USER_BLACKLIST_PREFIX + userId;
            long now = System.currentTimeMillis();
            long maxTtl = jwtProperties.getRememberMeExpiration();
            redisTemplate.opsForValue().set(key, String.valueOf(now), maxTtl, TimeUnit.MILLISECONDS);
            userBlacklistCache.put(userId, now);
            log.debug("用户所有Token已加入黑名单: userId={}, blacklistedAt={}", userId, now);
        } catch (Exception e) {
            log.error("将用户所有Token加入黑名单失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public boolean isTokenBlacklisted(String jti) {
        try {
            String key = TOKEN_BLACKLIST_PREFIX + jti;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查Token黑名单失败: jti={}, error={}", jti, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isUserBlacklisted(Long userId, long issuedAt) {
        try {
            Long blacklistedAt = userBlacklistCache.getIfPresent(userId);
            if (blacklistedAt == null) {
                String key = USER_BLACKLIST_PREFIX + userId;
                String value = redisTemplate.opsForValue().get(key);
                if (value == null) {
                    return false;
                }
                blacklistedAt = Long.parseLong(value);
                userBlacklistCache.put(userId, blacklistedAt);
            }
            return blacklistedAt > (issuedAt - 5000);
        } catch (Exception e) {
            log.error("检查用户级黑名单失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }
}
