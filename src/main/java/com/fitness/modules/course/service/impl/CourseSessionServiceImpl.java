package com.fitness.modules.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.mapper.CourseSessionMapper;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.entity.Course;
import com.fitness.modules.course.model.entity.CourseSession;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import com.fitness.modules.course.service.CourseSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSessionServiceImpl implements CourseSessionService {

    private final CourseSessionMapper sessionMapper;
    private final CourseMapper courseMapper;

    @Override
    public Page<CourseSessionVO> getSessionList(CourseQueryDTO query) {
        Page<CourseSession> page = new Page<>(query.getPageNum(), query.getPageSize());
        return sessionMapper.selectSessionList(page, query);
    }

    @Override
    public CourseSessionVO getSessionDetail(Long sessionId) {
        CourseSessionVO vo = sessionMapper.selectSessionDetail(sessionId);
        if (vo == null) {
            throw new RuntimeException("课程实例不存在");
        }
        return vo;
    }

    @Override
    public List<CourseSessionVO> getUpcomingSessions(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return sessionMapper.selectUpcomingSessions(limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateFutureSessions(int weeksAhead) {
        log.info("开始生成未来{}周的课程实例", weeksAhead);

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getDeleted, false)
               .eq(Course::getStatus, 1)
               .isNotNull(Course::getDayOfWeek)
               .isNotNull(Course::getStartTime);

        List<Course> activeCourses = courseMapper.selectList(wrapper);
        log.info("找到{}个活跃的周期性课程模板", activeCourses.size());

        for (Course course : activeCourses) {
            try {
                generateSessionsForCourse(course.getId(), weeksAhead);
            } catch (Exception e) {
                log.error("生成课程实例失败: courseId={}, error={}", course.getId(), e.getMessage(), e);
            }
        }

        log.info("课程实例生成完成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateSessionsForCourse(Long courseId, int weeksAhead) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || Boolean.TRUE.equals(course.getDeleted())) {
            return;
        }
        if (course.getDayOfWeek() == null || course.getStartTime() == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusWeeks(weeksAhead);

        List<LocalDate> existingDates = sessionMapper.selectSessionsByCourseId(courseId, maxDate)
                .stream()
                .map(CourseSessionVO::getSessionDate)
                .collect(Collectors.toList());
        Set<LocalDate> existingSet = new HashSet<>(existingDates);

        int targetDayOfWeek = course.getDayOfWeek();
        DayOfWeek targetDow = DayOfWeek.of(targetDayOfWeek);

        int generated = 0;
        for (int week = 0; week <= weeksAhead; week++) {
            LocalDate targetDate = today.plusWeeks(week)
                    .with(TemporalAdjusters.nextOrSame(targetDow));

            if (targetDate.isBefore(today)) {
                continue;
            }
            if (targetDate.isAfter(maxDate)) {
                break;
            }
            if (existingSet.contains(targetDate)) {
                continue;
            }

            CourseSession session = new CourseSession();
            session.setCourseId(courseId);
            session.setSessionDate(targetDate);
            session.setDayOfWeek(course.getDayOfWeek());
            session.setStartTime(course.getStartTime());
            session.setEndTime(course.getEndTime());
            session.setStatus(0);
            session.setCapacity(course.getCapacity() != null ? course.getCapacity() : 20);
            session.setBookedCount(0);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());

            sessionMapper.insert(session);
            generated++;
        }

        if (generated > 0) {
            log.info("课程[{}]生成了{}个新实例, 范围至{}", courseId, generated, maxDate);
        }
    }
}
