package com.fitness.modules.booking.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.booking.model.dto.BookingCancelDTO;
import com.fitness.modules.booking.model.dto.BookingDTO;
import com.fitness.modules.booking.model.vo.BookingListVO;
import com.fitness.modules.booking.model.vo.BookingVO;
import com.fitness.modules.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "课程预约", description = "课程预约用户端接口")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "创建预约")
    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Long> createBooking(@Valid @RequestBody BookingDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("创建预约请求: userId={}, courseId={}", userId, dto.getCourseId());
        Long bookingId = bookingService.createBooking(userId, dto);
        return Result.success(bookingId);
    }

    @Operation(summary = "取消预约")
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelBooking(@PathVariable Long bookingId,
                                      @Valid @RequestBody BookingCancelDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("取消预约请求: userId={}, bookingId={}", userId, bookingId);
        bookingService.cancelBooking(userId, bookingId, dto);
        return Result.success();
    }

    @Operation(summary = "获取我的预约列表")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<List<BookingListVO>> getMyBookings() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取我的预约列表: userId={}", userId);
        List<BookingListVO> list = bookingService.getMyBookings(userId);
        return Result.success(list);
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public Result<BookingVO> getBookingDetail(@PathVariable Long bookingId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取预约详情: userId={}, bookingId={}", userId, bookingId);
        BookingVO booking = bookingService.getBookingDetail(userId, bookingId);
        return Result.success(booking);
    }
}
