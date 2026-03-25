package com.fitness.modules.booking.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.booking.model.dto.BookingCancelDTO;
import com.fitness.modules.booking.model.dto.BookingDTO;
import com.fitness.modules.booking.model.vo.BookingListVO;
import com.fitness.modules.booking.model.vo.BookingVO;
import com.fitness.modules.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约控制器（用户端）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * 创建预约
     *
     * @param dto 预约信息
     * @return 预约ID
     */
    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Long> createBooking(@Valid @RequestBody BookingDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("创建预约请求: userId={}, courseId={}", userId, dto.getCourseId());
        Long bookingId = bookingService.createBooking(userId, dto);
        return Result.success(bookingId);
    }

    /**
     * 取消预约
     *
     * @param bookingId 预约ID
     * @param dto       取消信息
     * @return 操作结果
     */
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelBooking(@PathVariable Long bookingId,
                                      @Valid @RequestBody BookingCancelDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("取消预约请求: userId={}, bookingId={}", userId, bookingId);
        bookingService.cancelBooking(userId, bookingId, dto);
        return Result.success();
    }

    /**
     * 获取我的预约列表
     *
     * @return 预约列表
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<List<BookingListVO>> getMyBookings() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取我的预约列表: userId={}", userId);
        List<BookingListVO> list = bookingService.getMyBookings(userId);
        return Result.success(list);
    }

    /**
     * 获取预约详情
     *
     * @param bookingId 预约ID
     * @return 预约详情
     */
    @GetMapping("/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public Result<BookingVO> getBookingDetail(@PathVariable Long bookingId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取预约详情: userId={}, bookingId={}", userId, bookingId);
        BookingVO booking = bookingService.getBookingDetail(userId, bookingId);
        return Result.success(booking);
    }
}
