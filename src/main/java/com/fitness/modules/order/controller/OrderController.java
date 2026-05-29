package com.fitness.modules.order.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.order.model.dto.OrderDTO;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.order.service.OrderService;
import com.fitness.modules.membership.model.vo.AlipayPayVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "统一订单", description = "统一订单相关接口")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("创建订单: userId={}, orderType={}", userId, dto.getOrderType());
        return Result.success(orderService.createOrder(dto, userId));
    }

    @Operation(summary = "订单支付")
    @PostMapping("/orders/{orderNo}/pay")
    @PreAuthorize("isAuthenticated()")
    public Result<AlipayPayVO> payOrder(@PathVariable String orderNo,
                                        @RequestParam(defaultValue = "ALIPAY") String payMethod) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("订单支付: orderNo={}, payMethod={}", orderNo, payMethod);
        return Result.success(orderService.payOrder(orderNo, payMethod, userId));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/orders/{orderNo}")
    @PreAuthorize("isAuthenticated()")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderNo) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取订单详情: orderNo={}", orderNo);
        return Result.success(orderService.getOrderDetail(orderNo, userId));
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public Result<List<OrderVO>> getUserOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取用户订单列表: userId={}", userId);
        return Result.success(orderService.getUserOrders(userId));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/orders/{orderNo}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("取消订单: orderNo={}, userId={}", orderNo, userId);
        orderService.cancelOrder(orderNo, userId);
        return Result.success();
    }
}
