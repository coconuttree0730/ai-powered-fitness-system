package com.fitness.modules.user.service;

public interface TokenBlacklistService {

    void blacklistToken(String jti, long ttlMillis);

    void blacklistAllUserTokens(Long userId);

    boolean isTokenBlacklisted(String jti);

    boolean isUserBlacklisted(Long userId, long issuedAt);
}
