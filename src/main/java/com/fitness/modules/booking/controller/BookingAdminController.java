package com.fitness.modules.booking.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.booking.model.dto.BookingQueryDTO;
import com.fitness.modules.booking.model.vo.BookingListVO;
import com.fitness.modules.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "预约管理", description = "预约管理后台接口")
public class BookingAdminController {

    private final BookingService bookingService;

    @Operation(summary = "分页查询预约列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Page<BookingListVO>> getBookingList(@Valid BookingQueryDTO query) {
        log.info("管理端查询预约列表: userId={}, courseId={}, status={}",
                query.getUserId(), query.getCourseId(), query.getStatus());
        Page<BookingListVO> page = bookingService.getBookingList(query);
        return Result.success(page);
    }

    @Operation(summary = "完成预约")
    @PutMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> completeBooking(@PathVariable Long bookingId) {
        log.info("完成预约请求: bookingId={}", bookingId);
        bookingService.completeBooking(bookingId);
        return Result.success();
    }
}
