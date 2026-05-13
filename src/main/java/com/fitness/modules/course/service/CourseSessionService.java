package com.fitness.modules.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import java.util.List;

public interface CourseSessionService {

    Page<CourseSessionVO> getSessionList(CourseQueryDTO query);

    CourseSessionVO getSessionDetail(Long sessionId);

    List<CourseSessionVO> getUpcomingSessions(Integer limit);

    void generateFutureSessions(int weeksAhead);

    void generateSessionsForCourse(Long courseId, int weeksAhead);
}
