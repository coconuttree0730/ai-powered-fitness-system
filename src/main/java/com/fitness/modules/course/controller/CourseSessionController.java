package com.fitness.modules.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import com.fitness.modules.course.service.CourseSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程节次", description = "课程节次相关接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/course-sessions")
@RequiredArgsConstructor
public class CourseSessionController {

    private final CourseSessionService sessionService;

    @Operation(summary = "获取课程节次列表")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<CourseSessionVO>> getSessionList(@Valid CourseQueryDTO query) {
        log.info("获取课程实例列表: dayOfWeek={}", query.getDayOfWeek());
        Page<CourseSessionVO> page = sessionService.getSessionList(query);
        return Result.success(page);
    }

    @Operation(summary = "获取课程节次详情")
    @GetMapping("/{sessionId}")
    public Result<CourseSessionVO> getSessionDetail(@PathVariable Long sessionId) {
        log.info("获取课程实例详情: sessionId={}", sessionId);
        CourseSessionVO vo = sessionService.getSessionDetail(sessionId);
        return Result.success(vo);
    }

    @Operation(summary = "获取即将开始的课程节次")
    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public Result<java.util.List<CourseSessionVO>> getUpcomingSessions(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        java.util.List<CourseSessionVO> sessions = sessionService.getUpcomingSessions(limit);
        return Result.success(sessions);
    }

    @Operation(summary = "手动触发未来课程实例生成")
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> generateFutureSessions(@RequestParam(defaultValue = "4") Integer weeksAhead) {
        log.info("手动触发生成未来{}周的课程实例", weeksAhead);
        sessionService.generateFutureSessions(weeksAhead);
        return Result.success();
    }

    @Operation(summary = "取消课程实例")
    @PutMapping("/{sessionId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> cancelSession(@PathVariable Long sessionId) {
        log.info("管理员取消课程实例: sessionId={}", sessionId);
        sessionService.cancelSession(sessionId);
        return Result.success();
    }
}
