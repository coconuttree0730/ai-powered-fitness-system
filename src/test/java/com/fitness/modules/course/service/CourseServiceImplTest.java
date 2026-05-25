package com.fitness.modules.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.service.impl.CourseServiceImpl;
import com.fitness.modules.ranking.service.RedisRankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private RedisRankingService redisRankingService;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void publicCourseSearchRecordsKeywordHotness() {
        ReflectionTestUtils.setField(courseService, "baseMapper", courseMapper);

        CourseQueryDTO query = new CourseQueryDTO();
        query.setKeyword("yoga");

        when(courseMapper.selectCourseList(any(Page.class), eq(query))).thenReturn(Page.of(1, 10));

        courseService.getPublicCourseList(query);

        verify(redisRankingService).recordSearchKeyword("course", "yoga");
    }
}
