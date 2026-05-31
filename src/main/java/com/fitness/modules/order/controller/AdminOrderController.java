package com.fitness.modules.order.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.order.service.OrderService;
import com.fitness.modules.product.service.ProductOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "订单管理（后台）", description = "后台订单管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final ProductOrderService productOrderService;

    @Operation(summary = "分页查询订单列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<OrderVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("管理端查询订单列表: page={}, pageSize={}, orderType={}, status={}, keyword={}", page, pageSize, orderType, status, keyword);
        return Result.success(orderService.getAdminOrderPage(page, pageSize, orderType, status, keyword, startTime, endTime));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        log.info("管理端获取订单详情: orderNo={}", orderNo);
        return Result.success(orderService.getAdminOrderDetail(orderNo));
    }

    @Operation(summary = "确认收款")
    @PutMapping("/{orderNo}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> confirm(@PathVariable String orderNo) {
        log.info("管理端确认收款: orderNo={}", orderNo);
        orderService.confirmPayment(orderNo);
        return Result.success();
    }

    @Operation(summary = "发货")
    @PutMapping("/{orderNo}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> ship(@PathVariable String orderNo, @RequestBody Map<String, String> body) {
        String trackingNo = body.get("trackingNo");
        String carrier = body.get("carrier");
        log.info("管理端发货: orderNo={}, trackingNo={}, carrier={}", orderNo, trackingNo, carrier);
        orderService.shipOrder(orderNo, trackingNo, carrier);
        return Result.success();
    }

    @Operation(summary = "确认取货")
    @PutMapping("/{orderNo}/pickup")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> pickup(@PathVariable String orderNo, @RequestBody Map<String, String> body) {
        String pickupCode = body.get("pickupCode");
        log.info("管理端确认取货: orderNo={}, pickupCode={}", orderNo, pickupCode);
        productOrderService.confirmPickup(orderNo, pickupCode);
        return Result.success();
    }

    @Operation(summary = "获取订单统计数据")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> stats() {
        log.info("管理端获取订单统计数据");
        return Result.success(orderService.getOrderStats());
    }
}