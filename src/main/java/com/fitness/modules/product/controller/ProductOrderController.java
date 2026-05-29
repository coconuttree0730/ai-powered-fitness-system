package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.membership.model.vo.AlipayPayVO;
import com.fitness.modules.order.service.OrderService;
import com.fitness.modules.product.model.dto.CalculatePriceDTO;
import com.fitness.modules.product.model.dto.ProductOrderDTO;
import com.fitness.modules.product.model.vo.PriceCalculationVO;
import com.fitness.modules.product.model.vo.ProductOrderVO;
import com.fitness.modules.product.service.ProductOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "商品订单", description = "商品订单相关接口")
@RestController
@RequestMapping("/api/v1/product-orders")
@RequiredArgsConstructor
public class ProductOrderController {

    private final ProductOrderService productOrderService;
    private final OrderService orderService;

    @Operation(summary = "计算订单价格")
    @PostMapping("/calculate")
    @PreAuthorize("isAuthenticated()")
    public Result<PriceCalculationVO> calculatePrice(@Valid @RequestBody CalculatePriceDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("计算商品订单价格: userId={}, productId={}, quantity={}", userId, dto.getProductId(), dto.getQuantity());
        return Result.success(productOrderService.calculatePrice(dto, userId));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<ProductOrderVO> createOrder(@Valid @RequestBody ProductOrderDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("创建商品订单: userId={}, productId={}, quantity={}", userId, dto.getProductId(), dto.getQuantity());
        return Result.success(productOrderService.createOrder(dto, userId));
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<ProductOrderVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取用户订单列表: userId={}", userId);
        return Result.success(productOrderService.getUserOrders(userId));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderNo}")
    @PreAuthorize("isAuthenticated()")
    public Result<ProductOrderVO> detail(@PathVariable String orderNo) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取订单详情: orderNo={}, userId={}", orderNo, userId);
        return Result.success(productOrderService.getOrderDetail(orderNo, userId));
    }

    @Operation(summary = "订单支付")
    @PostMapping("/{orderNo}/pay")
    @PreAuthorize("isAuthenticated()")
    public Result<AlipayPayVO> pay(@PathVariable String orderNo,
            @RequestParam(defaultValue = "ALIPAY") String payMethod) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("订单支付: orderNo={}, payMethod={}", orderNo, payMethod);
        return Result.success(orderService.payOrder(orderNo, payMethod, userId));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderNo}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancel(@PathVariable String orderNo) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("取消商品订单: orderNo={}, userId={}", orderNo, userId);
        productOrderService.cancelOrder(orderNo, userId);
        return Result.success();
    }
}
