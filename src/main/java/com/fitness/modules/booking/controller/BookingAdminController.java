package com.fitness.modules.booking.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.booking.model.dto.BookingQueryDTO;
import com.fitness.modules.booking.model.vo.BookingListVO;
import com.fitness.modules.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 预约管理控制器（管理端）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
public class BookingAdminController {

    private final BookingService bookingService;

    /**
     * 分页查询预约列表
     *
     * @param query 查询条件
     * @return 预约列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Page<BookingListVO>> getBookingList(@Valid BookingQueryDTO query) {
        log.info("管理端查询预约列表: userId={}, courseId={}, status={}",
                query.getUserId(), query.getCourseId(), query.getStatus());
        Page<BookingListVO> page = bookingService.getBookingList(query);
        return Result.success(page);
    }

    /**
     * 确认预约
     *
     * @param bookingId 预约ID
     * @return 操作结果
     */
    @PutMapping("/{bookingId}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> confirmBooking(@PathVariable Long bookingId) {
        log.info("确认预约请求: bookingId={}", bookingId);
        bookingService.confirmBooking(bookingId);
        return Result.success();
    }

    /**
     * 拒绝预约
     *
     * @param bookingId 预约ID
     * @return 操作结果
     */
    @PutMapping("/{bookingId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> rejectBooking(@PathVariable Long bookingId) {
        log.info("拒绝预约请求: bookingId={}", bookingId);
        bookingService.rejectBooking(bookingId);
        return Result.success();
    }

    /**
     * 完成预约
     *
     * @param bookingId 预约ID
     * @return 操作结果
     */
    @PutMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> completeBooking(@PathVariable Long bookingId) {
        log.info("完成预约请求: bookingId={}", bookingId);
        bookingService.completeBooking(bookingId);
        return Result.success();
    }
}
