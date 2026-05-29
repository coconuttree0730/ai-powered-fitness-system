package com.fitness.modules.user.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.user.model.vo.CoachNotificationVO;
import com.fitness.modules.user.service.CoachNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教练通知", description = "教练通知接口")
@RestController
@RequestMapping("/api/v1/coach/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class CoachNotificationController {

    private final CoachNotificationService coachNotificationService;

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long coachId = SecurityUtils.getCurrentUserId();
        return Result.success(coachNotificationService.getUnreadCount(coachId));
    }

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<List<CoachNotificationVO>> getNotifications() {
        Long coachId = SecurityUtils.getCurrentUserId();
        return Result.success(coachNotificationService.getNotifications(coachId));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long coachId = SecurityUtils.getCurrentUserId();
        coachNotificationService.markAsRead(id, coachId);
        return Result.success();
    }
}