package com.fitness.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;

@Slf4j
@Component
public class RedisTemplateCacheSupport {

    public static final String NULL_MARKER = "__NULL__";

    private static final String CACHE_KEY_PREFIX = "fitness:manual-cache:";
    private static final String LOCK_KEY_SUFFIX = ":lock";
    private static final Duration NULL_TTL = Duration.ofMinutes(2);
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final int MAX_RETRY_TIMES = 3;
    private static final long RETRY_SLEEP_MILLIS = 50L;
    private static final Map<String, Duration> CACHE_TTLS = buildCacheTtls();
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """,
            Long.class
    );

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisTemplateCacheSupport(RedisTemplate<String, Object> redisTemplate,
                                     StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public <T> T getOrLoad(String cacheName, String businessKey, Supplier<T> loader) {
        String cacheKey = buildCacheKey(cacheName, businessKey);
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        CacheLookupResult<T> lookupResult = resolveCachedValue(cachedValue);
        if (lookupResult.hit()) {
            log.debug("[CACHE HIT] cache={}, key={}", cacheName, businessKey);
            return lookupResult.value();
        }

        log.debug("[CACHE MISS] cache={}, key={}", cacheName, businessKey);
        String lockKey = cacheKey + LOCK_KEY_SUFFIX;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_TTL);
        if (Boolean.TRUE.equals(locked)) {
            try {
                Object doubleChecked = redisTemplate.opsForValue().get(cacheKey);
                CacheLookupResult<T> doubleCheckResult = resolveCachedValue(doubleChecked);
                if (doubleCheckResult.hit()) {
                    log.debug("[CACHE HIT] cache={}, key={}, source=double-check", cacheName, businessKey);
                    return doubleCheckResult.value();
                }
                return loadAndCache(cacheName, businessKey, cacheKey, loader);
            } finally {
                unlock(lockKey, lockValue);
            }
        }

        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            sleepQuietly(RETRY_SLEEP_MILLIS * (i + 1));
            Object retriedValue = redisTemplate.opsForValue().get(cacheKey);
            CacheLookupResult<T> retryResult = resolveCachedValue(retriedValue);
            if (retryResult.hit()) {
                log.debug("[CACHE HIT] cache={}, key={}, source=retry{}", cacheName, businessKey, i + 1);
                return retryResult.value();
            }
        }

        log.debug("[CACHE BYPASS] cache={}, key={}, reason=lock-timeout", cacheName, businessKey);
        return loadAndCache(cacheName, businessKey, cacheKey, loader);
    }

    public void evict(String cacheName, String businessKey) {
        String cacheKey = buildCacheKey(cacheName, businessKey);
        redisTemplate.delete(cacheKey);
        log.debug("[CACHE EVICT] cache={}, key={}", cacheName, businessKey);
    }

    public void evictAll(String cacheName) {
        String prefix = CACHE_KEY_PREFIX + cacheName + ":";
        Set<String> keys = scanKeys(prefix + "*");
        if (keys.isEmpty()) {
            return;
        }
        redisTemplate.delete(keys);
        log.debug("[CACHE CLEAR] cache={}, count={}", cacheName, keys.size());
    }

    private <T> T loadAndCache(String cacheName, String businessKey, String cacheKey, Supplier<T> loader) {
        T loadedValue = loader.get();
        if (loadedValue == null) {
            redisTemplate.opsForValue().set(cacheKey, NULL_MARKER, NULL_TTL);
            log.debug("[CACHE NULL] cache={}, key={}, ttl={}s", cacheName, businessKey, NULL_TTL.toSeconds());
            return null;
        }

        Duration ttl = resolveTtlWithJitter(cacheName);
        redisTemplate.opsForValue().set(cacheKey, loadedValue, ttl);
        log.debug("[CACHE LOAD] cache={}, key={}, ttl={}s", cacheName, businessKey, ttl.toSeconds());
        return loadedValue;
    }

    private void unlock(String lockKey, String lockValue) {
        stringRedisTemplate.execute(UNLOCK_SCRIPT, java.util.List.of(lockKey), lockValue);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Duration resolveTtlWithJitter(String cacheName) {
        Duration baseTtl = CACHE_TTLS.getOrDefault(cacheName, Duration.ofMinutes(10));
        long extraSeconds = ThreadLocalRandom.current().nextLong(0, 121);
        return baseTtl.plusSeconds(extraSeconds);
    }

    private Set<String> scanKeys(String pattern) {
        return Objects.requireNonNullElseGet(redisTemplate.execute(new RedisCallback<Set<String>>() {
            @Override
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<String> keys = new HashSet<>();
                ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    while (cursor.hasNext()) {
                        keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("scan redis keys failed", e);
                }
                return keys;
            }
        }), HashSet::new);
    }

    private String buildCacheKey(String cacheName, String businessKey) {
        return CACHE_KEY_PREFIX + cacheName + ":" + businessKey;
    }

    @SuppressWarnings("unchecked")
    private <T> CacheLookupResult<T> resolveCachedValue(Object cachedValue) {
        if (cachedValue == null) {
            return CacheLookupResult.miss();
        }
        if (NULL_MARKER.equals(cachedValue)) {
            return CacheLookupResult.hit(null);
        }
        return CacheLookupResult.hit((T) cachedValue);
    }

    private static Map<String, Duration> buildCacheTtls() {
        Map<String, Duration> ttlMap = new HashMap<>();
        ttlMap.put(RedisCacheNames.ACTIVE_BANNERS, Duration.ofMinutes(30));
        ttlMap.put(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS, Duration.ofMinutes(15));
        ttlMap.put(RedisCacheNames.DICT_OPTIONS, Duration.ofMinutes(60));
        ttlMap.put(RedisCacheNames.COURSE_PUBLIC_LIST, Duration.ofMinutes(10));
        ttlMap.put(RedisCacheNames.COURSE_CATEGORIES, Duration.ofMinutes(30));
        ttlMap.put(RedisCacheNames.COURSE_HOME_CATEGORIES, Duration.ofMinutes(10));
        ttlMap.put(RedisCacheNames.COURSE_HOME_CARDS, Duration.ofMinutes(10));
        ttlMap.put(RedisCacheNames.UPCOMING_SESSIONS, Duration.ofMinutes(10));
        ttlMap.put(RedisCacheNames.COACH_HOME, Duration.ofMinutes(15));
        ttlMap.put(RedisCacheNames.PRODUCT_PUBLIC_LIST, Duration.ofMinutes(10));
        ttlMap.put(RedisCacheNames.MEMBERSHIP_ACTIVE_CARDS, Duration.ofMinutes(30));
        ttlMap.put(RedisCacheNames.MEMBERSHIP_RECOMMEND_CARDS, Duration.ofMinutes(15));
        ttlMap.put(RedisCacheNames.USER_PERMISSIONS, Duration.ofMinutes(30));
        ttlMap.put(RedisCacheNames.USER_ROLES, Duration.ofMinutes(30));
        return ttlMap;
    }

    private record CacheLookupResult<T>(boolean hit, T value) {

        private static <T> CacheLookupResult<T> hit(T value) {
            return new CacheLookupResult<>(true, value);
        }

        private static <T> CacheLookupResult<T> miss() {
            return new CacheLookupResult<>(false, null);
        }
    }
}
