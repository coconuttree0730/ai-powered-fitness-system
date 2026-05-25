package com.fitness.modules.ranking.service.impl;

import com.fitness.modules.ranking.service.RedisRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRankingServiceImpl implements RedisRankingService {

    private static final String COURSE_BOOKING_KEY = "ranking:course:booking";
    private static final String COACH_BOOKING_KEY = "ranking:coach:booking";
    private static final String PRODUCT_SALES_KEY = "ranking:product:sales";
    private static final String ANNOUNCEMENT_VIEW_KEY = "ranking:announcement:view";
    private static final String SEARCH_KEY_PREFIX = "ranking:search:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void incrementCourseBookingScore(Long courseId, double delta) {
        if (courseId == null || delta == 0D) {
            return;
        }
        stringRedisTemplate.opsForZSet().incrementScore(COURSE_BOOKING_KEY, String.valueOf(courseId), delta);
        log.debug("[RANKING UPDATE] type=course_booking, targetId={}, delta={}", courseId, delta);
    }

    @Override
    public void incrementCoachBookingScore(Long coachId, double delta) {
        if (coachId == null || delta == 0D) {
            return;
        }
        stringRedisTemplate.opsForZSet().incrementScore(COACH_BOOKING_KEY, String.valueOf(coachId), delta);
        log.debug("[RANKING UPDATE] type=coach_booking, targetId={}, delta={}", coachId, delta);
    }

    @Override
    public void incrementProductSalesScore(Long productId, double delta) {
        if (productId == null || delta == 0D) {
            return;
        }
        stringRedisTemplate.opsForZSet().incrementScore(PRODUCT_SALES_KEY, String.valueOf(productId), delta);
        log.debug("[RANKING UPDATE] type=product_sales, targetId={}, delta={}", productId, delta);
    }

    @Override
    public void incrementAnnouncementViewScore(Long announcementId, double delta) {
        if (announcementId == null || delta == 0D) {
            return;
        }
        stringRedisTemplate.opsForZSet().incrementScore(ANNOUNCEMENT_VIEW_KEY, String.valueOf(announcementId), delta);
        log.debug("[RANKING UPDATE] type=announcement_view, targetId={}, delta={}", announcementId, delta);
    }

    @Override
    public void recordSearchKeyword(String scene, String keyword) {
        if (!StringUtils.hasText(scene) || !StringUtils.hasText(keyword)) {
            return;
        }
        String normalizedKeyword = keyword.trim().toLowerCase();
        if (!StringUtils.hasText(normalizedKeyword)) {
            return;
        }
        stringRedisTemplate.opsForZSet()
                .incrementScore(SEARCH_KEY_PREFIX + scene.trim().toLowerCase(), normalizedKeyword, 1D);
        log.debug("[RANKING UPDATE] type=search_hotword, scene={}, keyword={}, delta=1.0", scene.trim().toLowerCase(), normalizedKeyword);
    }
}
