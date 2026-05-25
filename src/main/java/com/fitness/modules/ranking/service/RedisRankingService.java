package com.fitness.modules.ranking.service;

public interface RedisRankingService {

    void incrementCourseBookingScore(Long courseId, double delta);

    void incrementCoachBookingScore(Long coachId, double delta);

    void incrementProductSalesScore(Long productId, double delta);

    void incrementAnnouncementViewScore(Long announcementId, double delta);

    void recordSearchKeyword(String scene, String keyword);
}
