package com.fitness.modules.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.integration.minio.service.FileService;
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseVO;
import com.fitness.modules.course.service.impl.CourseServiceImpl;
import com.fitness.modules.ranking.service.RedisRankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private FileService fileService;

    @Mock
    private RedisRankingService redisRankingService;

    @Mock
    private RedisTemplateCacheSupport redisTemplateCacheSupport;

    @InjectMocks
    private CourseServiceImpl courseService;

    @SuppressWarnings("unchecked")
    @Test
    void publicCourseSearchRecordsKeywordHotness() {
        CourseQueryDTO query = new CourseQueryDTO();
        query.setKeyword("yoga");

        when(redisTemplateCacheSupport.getOrLoad(any(), any(), any()))
                .thenReturn(Page.of(1, 10));

        courseService.getPublicCourseList(query);

        verify(redisRankingService).recordSearchKeyword("course", "yoga");
    }
}
