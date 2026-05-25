package com.fitness.common.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisTemplateCacheSupportTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, Object> objectValueOperations;

    @Mock
    private ValueOperations<String, String> stringValueOperations;

    @Test
    void getOrLoadReturnsCachedValueWithoutCallingLoader() {
        when(redisTemplate.opsForValue()).thenReturn(objectValueOperations);
        when(objectValueOperations.get("fitness:manual-cache:course:categories:all"))
                .thenReturn(List.of("瑜伽", "普拉提"));

        RedisTemplateCacheSupport cacheSupport = new RedisTemplateCacheSupport(redisTemplate, stringRedisTemplate);

        AtomicInteger loaderCalls = new AtomicInteger();
        Object result = cacheSupport.getOrLoad(
                RedisCacheNames.COURSE_CATEGORIES,
                "all",
                () -> {
                    loaderCalls.incrementAndGet();
                    return List.of("should-not-run");
                }
        );

        assertEquals(List.of("瑜伽", "普拉提"), result);
        assertEquals(0, loaderCalls.get());
        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void getOrLoadCachesLoadedValueWithJitteredTtl() {
        when(redisTemplate.opsForValue()).thenReturn(objectValueOperations);
        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOperations);
        when(objectValueOperations.get("fitness:manual-cache:course:categories:all"))
                .thenReturn(null, null);
        when(stringValueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        RedisTemplateCacheSupport cacheSupport = new RedisTemplateCacheSupport(redisTemplate, stringRedisTemplate);

        List<String> loaded = List.of("拳击", "瑜伽");
        Object result = cacheSupport.getOrLoad(RedisCacheNames.COURSE_CATEGORIES, "all", () -> loaded);

        assertEquals(loaded, result);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(objectValueOperations).set(
                eq("fitness:manual-cache:course:categories:all"),
                eq(loaded),
                ttlCaptor.capture()
        );
        Duration actualTtl = ttlCaptor.getValue();
        assertTrue(!actualTtl.minusMinutes(30).isNegative());
        assertTrue(!Duration.ofMinutes(32).minus(actualTtl).isNegative());
    }

    @Test
    void getOrLoadCachesNullMarkerToPreventPenetration() {
        when(redisTemplate.opsForValue()).thenReturn(objectValueOperations);
        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOperations);
        when(objectValueOperations.get("fitness:manual-cache:announcement:published:missing"))
                .thenReturn(null, null, RedisTemplateCacheSupport.NULL_MARKER);
        when(stringValueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        RedisTemplateCacheSupport cacheSupport = new RedisTemplateCacheSupport(redisTemplate, stringRedisTemplate);

        AtomicInteger loaderCalls = new AtomicInteger();
        Object first = cacheSupport.getOrLoad(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS, "missing", () -> {
            loaderCalls.incrementAndGet();
            return null;
        });
        Object second = cacheSupport.getOrLoad(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS, "missing", () -> {
            loaderCalls.incrementAndGet();
            return List.of("should-not-run");
        });

        assertNull(first);
        assertNull(second);
        assertEquals(1, loaderCalls.get());
        verify(objectValueOperations).set(
                eq("fitness:manual-cache:announcement:published:missing"),
                eq(RedisTemplateCacheSupport.NULL_MARKER),
                eq(Duration.ofMinutes(2))
        );
    }
}
