package com.fitness.modules.announcement.service;

import com.fitness.modules.announcement.mapper.AnnouncementMapper;
import com.fitness.modules.announcement.service.impl.AnnouncementServiceImpl;
import com.fitness.modules.ranking.service.RedisRankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceImplTest {

    @Mock
    private AnnouncementMapper announcementMapper;

    @Mock
    private RedisRankingService redisRankingService;

    @InjectMocks
    private AnnouncementServiceImpl announcementService;

    @Test
    void incrementViewCountUpdatesDatabaseAndRanking() {
        ReflectionTestUtils.setField(announcementService, "baseMapper", announcementMapper);

        assertDoesNotThrow(() -> announcementService.incrementViewCount(9L));

        verify(announcementMapper).incrementViewCount(9L);
        verify(redisRankingService).incrementAnnouncementViewScore(9L, 1D);
    }
}
