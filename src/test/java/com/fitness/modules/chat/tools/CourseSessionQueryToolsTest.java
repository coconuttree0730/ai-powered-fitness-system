package com.fitness.modules.chat.tools;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import com.fitness.modules.course.service.CourseSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseSessionQueryToolsTest {

    @Mock
    private CourseSessionService courseSessionService;

    @InjectMocks
    private CourseSessionQueryTools courseSessionQueryTools;

    @Test
    void listUpcomingSessionsShouldReturnServiceData() {
        CourseSessionVO session = new CourseSessionVO();
        session.setId(1L);
        session.setCourseName("瑜伽课");
        session.setSessionDate(LocalDate.now().plusDays(1));
        session.setStartTime(LocalTime.of(10, 0));
        session.setRemainingCount(5);

        Page<CourseSessionVO> page = new Page<>(1, 10);
        page.setRecords(List.of(session));
        when(courseSessionService.getSessionList(any(CourseQueryDTO.class))).thenReturn(page);

        List<CourseSessionVO> result = courseSessionQueryTools.listUpcomingSessions(null, 7);

        assertEquals(1, result.size());
        assertEquals("瑜伽课", result.get(0).getCourseName());
    }

    @Test
    void listSessionsByCourseShouldReturnServiceData() {
        CourseSessionVO session = new CourseSessionVO();
        session.setId(1L);
        session.setCourseId(100L);
        session.setCourseName("普拉提");
        session.setSessionDate(LocalDate.now().plusDays(2));

        Page<CourseSessionVO> page = new Page<>(1, 10);
        page.setRecords(List.of(session));
        when(courseSessionService.getSessionList(any(CourseQueryDTO.class))).thenReturn(page);

        List<CourseSessionVO> result = courseSessionQueryTools.listSessionsByCourse(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getCourseId());
    }

    @Test
    void listSessionsByDateShouldReturnServiceData() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        CourseSessionVO session = new CourseSessionVO();
        session.setId(1L);
        session.setSessionDate(targetDate);

        Page<CourseSessionVO> page = new Page<>(1, 10);
        page.setRecords(List.of(session));
        when(courseSessionService.getSessionList(any(CourseQueryDTO.class))).thenReturn(page);

        List<CourseSessionVO> result = courseSessionQueryTools.listSessionsByDate(targetDate);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
