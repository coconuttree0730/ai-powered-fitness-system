package com.fitness.modules.booking.service.impl;

import com.fitness.modules.booking.mapper.BookingMapper;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisBookingReservationService implements BookingReservationService {

    private static final String STOCK_KEY_PREFIX = "booking:session:stock:";
    private static final String USERS_KEY_PREFIX = "booking:session:users:";
    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
                return 1
            end
            local stock = tonumber(redis.call('GET', KEYS[1]))
            if stock == nil or stock <= 0 then
                return 2
            end
            redis.call('DECR', KEYS[1])
            redis.call('SADD', KEYS[2], ARGV[1])
            return 0
            """,
            Long.class
    );
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            """
            local removed = redis.call('SREM', KEYS[2], ARGV[1])
            if removed == 1 and redis.call('EXISTS', KEYS[1]) == 1 then
                redis.call('INCR', KEYS[1])
            end
            return removed
            """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final BookingMapper bookingMapper;

    @Override
    public BookingReservationResult tryReserve(Long sessionId, Long userId, CourseSessionVO session) {
        Duration ttl = resolveTtl(session);
        initializeReservationCache(sessionId, session, ttl);
        Long result = stringRedisTemplate.execute(
                RESERVE_SCRIPT,
                List.of(stockKey(sessionId), usersKey(sessionId)),
                String.valueOf(userId)
        );
        if (result == null || result == 0L) {
            touchTtl(sessionId, ttl);
            log.debug("[REDIS RESERVE] sessionId={}, userId={}, result=SUCCESS", sessionId, userId);
            return BookingReservationResult.success();
        }
        if (result == 1L) {
            log.debug("[REDIS RESERVE] sessionId={}, userId={}, result=ALREADY_BOOKED", sessionId, userId);
            return BookingReservationResult.alreadyBooked();
        }
        log.debug("[REDIS RESERVE] sessionId={}, userId={}, result=FULL", sessionId, userId);
        return BookingReservationResult.full();
    }

    @Override
    public void releaseReservation(Long sessionId, Long userId, CourseSessionVO session) {
        Duration ttl = resolveTtl(session);
        Long removed = stringRedisTemplate.execute(
                RELEASE_SCRIPT,
                List.of(stockKey(sessionId), usersKey(sessionId)),
                String.valueOf(userId)
        );
        touchTtl(sessionId, ttl);
        log.debug("[REDIS RELEASE] sessionId={}, userId={}, released={}", sessionId, userId, removed != null && removed > 0);
    }

    private void initializeReservationCache(Long sessionId, CourseSessionVO session, Duration ttl) {
        int remaining = Math.max(0, (session.getCapacity() == null ? 0 : session.getCapacity())
                - (session.getBookedCount() == null ? 0 : session.getBookedCount()));
        Boolean initialized = stringRedisTemplate.opsForValue()
                .setIfAbsent(stockKey(sessionId), String.valueOf(remaining), ttl);
        if (Boolean.TRUE.equals(initialized)) {
            List<Long> userIds = bookingMapper.selectActiveUserIdsBySessionId(sessionId);
            if (!CollectionUtils.isEmpty(userIds)) {
                stringRedisTemplate.opsForSet().add(
                        usersKey(sessionId),
                        userIds.stream().map(String::valueOf).toArray(String[]::new)
                );
            }
            touchTtl(sessionId, ttl);
            log.debug("[REDIS INIT] sessionId={}, remainingStock={}, activeUserCount={}",
                    sessionId, remaining, userIds == null ? 0 : userIds.size());
            return;
        }
        touchTtl(sessionId, ttl);
    }

    private void touchTtl(Long sessionId, Duration ttl) {
        stringRedisTemplate.expire(stockKey(sessionId), ttl);
        stringRedisTemplate.expire(usersKey(sessionId), ttl);
    }

    private Duration resolveTtl(CourseSessionVO session) {
        LocalDateTime expireAt = LocalDateTime.of(
                session.getSessionDate(),
                session.getEndTime() != null ? session.getEndTime() : LocalTime.MAX
        ).plusDays(1);
        Duration ttl = Duration.between(LocalDateTime.now(), expireAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return Duration.ofDays(1);
        }
        return ttl;
    }

    private String stockKey(Long sessionId) {
        return STOCK_KEY_PREFIX + sessionId;
    }

    private String usersKey(Long sessionId) {
        return USERS_KEY_PREFIX + sessionId;
    }
}
