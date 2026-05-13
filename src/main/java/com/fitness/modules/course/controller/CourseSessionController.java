package com.fitness.modules.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import com.fitness.modules.course.service.CourseSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/course-sessions")
@RequiredArgsConstructor
public class CourseSessionController {

    private final CourseSessionService sessionService;

    @GetMapping("/list")
    public Result<Page<CourseSessionVO>> getSessionList(CourseQueryDTO query) {
        log.info("获取课程实例列表: dayOfWeek={}", query.getDayOfWeek());
        Page<CourseSessionVO> page = sessionService.getSessionList(query);
        return Result.success(page);
    }

    @GetMapping("/{sessionId}")
    public Result<CourseSessionVO> getSessionDetail(@PathVariable Long sessionId) {
        log.info("获取课程实例详情: sessionId={}", sessionId);
        CourseSessionVO vo = sessionService.getSessionDetail(sessionId);
        return Result.success(vo);
    }

    @GetMapping("/upcoming")
    public Result<java.util.List<CourseSessionVO>> getUpcomingSessions(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        java.util.List<CourseSessionVO> sessions = sessionService.getUpcomingSessions(limit);
        return Result.success(sessions);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> generateFutureSessions(@RequestParam(defaultValue = "4") Integer weeksAhead) {
        log.info("手动触发生成未来{}周的课程实例", weeksAhead);
        sessionService.generateFutureSessions(weeksAhead);
        return Result.success();
    }
}
