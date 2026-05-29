package com.fitness.modules.booking.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.booking.model.dto.PrivateCoachBookingDTO;
import com.fitness.modules.booking.model.vo.PrivateCoachBookingVO;
import com.fitness.modules.booking.service.PrivateCoachBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "私教课程预约", description = "私教课程预约接口")
public class PrivateCoachBookingController {

    private final PrivateCoachBookingService service;

    // ==================== 会员端接口 ====================

    @Operation(summary = "创建私教预约")
    @PostMapping("/api/v1/private-coach-bookings")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Long> createBooking(@Valid @RequestBody PrivateCoachBookingDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("创建私教预约: userId={}, coachId={}, date={}", userId, dto.getCoachId(), dto.getBookingDate());
        Long bookingId = service.createBooking(userId, dto);
        return Result.success(bookingId);
    }

    @Operation(summary = "获取我的私教预约列表")
    @GetMapping("/api/v1/private-coach-bookings/my")
    @PreAuthorize("isAuthenticated()")
    public Result<List<PrivateCoachBookingVO>> getMyBookings() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取我的私教预约列表: userId={}", userId);
        List<PrivateCoachBookingVO> list = service.getMyBookings(userId);
        return Result.success(list);
    }

    @Operation(summary = "取消私教预约")
    @PutMapping("/api/v1/private-coach-bookings/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelBooking(@PathVariable Long id,
                                      @RequestBody CancelReasonBody body) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("取消私教预约: userId={}, bookingId={}", userId, id);
        service.cancelBooking(userId, id, body != null ? body.getCancelReason() : null);
        return Result.success();
    }

    // ==================== 教练端接口 ====================

    @Operation(summary = "按日期范围查询私教预约")
    @GetMapping("/api/v1/coach/private-coach-bookings/by-range")
    @PreAuthorize("hasRole('COACH')")
    public Result<List<PrivateCoachBookingVO>> getBookingsByRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long coachId = SecurityUtils.getCurrentUserId();
        log.info("教练查询预约范围: coachId={}, {} ~ {}", coachId, startDate, endDate);
        List<PrivateCoachBookingVO> list = service.getCoachBookingsByDateRange(coachId, startDate, endDate);
        return Result.success(list);
    }

    @Operation(summary = "会员按教练查询日期范围预约（日历加载用）")
    @GetMapping("/api/v1/private-coach-bookings/coach/{coachId}/by-range")
    @PreAuthorize("isAuthenticated()")
    public Result<List<PrivateCoachBookingVO>> getCoachBookingsByRangeForMember(
            @PathVariable Long coachId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("会员查询教练预约范围: coachId={}, {} ~ {}", coachId, startDate, endDate);
        List<PrivateCoachBookingVO> list = service.getCoachBookingsByDateRange(coachId, startDate, endDate);
        return Result.success(list);
    }

    @Operation(summary = "确认私教预约")
    @PutMapping("/api/v1/coach/private-coach-bookings/{id}/confirm")
    @PreAuthorize("hasRole('COACH')")
    public Result<Void> confirmBooking(@PathVariable Long id) {
        Long coachId = SecurityUtils.getCurrentUserId();
        log.info("教练确认预约: coachId={}, bookingId={}", coachId, id);
        service.confirmBooking(coachId, id);
        return Result.success();
    }

    @Operation(summary = "完成私教预约")
    @PutMapping("/api/v1/coach/private-coach-bookings/{id}/complete")
    @PreAuthorize("hasRole('COACH')")
    public Result<Void> completeBooking(@PathVariable Long id) {
        log.info("完成预约: bookingId={}", id);
        service.completeBooking(id);
        return Result.success();
    }

    @lombok.Data
    private static class CancelReasonBody {
        private String cancelReason;
    }
}