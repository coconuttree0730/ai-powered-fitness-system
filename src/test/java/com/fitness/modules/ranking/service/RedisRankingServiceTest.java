package com.fitness.modules.ranking.service;

import com.fitness.modules.ranking.service.impl.RedisRankingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRankingServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private RedisRankingServiceImpl rankingService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        rankingService = new RedisRankingServiceImpl(stringRedisTemplate);
    }

    @Test
    void recordSearchKeywordWritesToScopedHotwordZset() {
        rankingService.recordSearchKeyword("course", "hiit");

        verify(zSetOperations).incrementScore(eq("ranking:search:course"), eq("hiit"), eq(1D));
    }
}
