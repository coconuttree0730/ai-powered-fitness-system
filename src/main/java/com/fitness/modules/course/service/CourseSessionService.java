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

    /**
     * 关闭过期 session：将已过期的待开始 session 标记为已结束，关联预约标记为已完成
     */
    void closeExpiredSessions();

    /**
     * 管理员取消单个 session：取消 session 及所有关联预约
     */
    void cancelSession(Long sessionId);
}
