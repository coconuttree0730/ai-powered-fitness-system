package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.product.model.vo.ProductOrderVO;
import com.fitness.modules.product.service.ProductOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "商品订单管理（后台）", description = "后台商品订单管理接口")
@RestController
@RequestMapping("/api/v1/admin/product-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductOrderAdminController {

    private final ProductOrderService productOrderService;

    @Operation(summary = "获取所有订单列表")
    @GetMapping
    public Result<List<ProductOrderVO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        log.info("获取商品订单列表: status={}, keyword={}", status, keyword);
        return Result.success(productOrderService.getAllOrders(status, keyword));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderNo}")
    public Result<ProductOrderVO> detail(@PathVariable String orderNo) {
        log.info("获取商品订单详情: orderNo={}", orderNo);
        return Result.success(productOrderService.getOrderDetail(orderNo));
    }

    @Operation(summary = "确认取货（扫码核销）")
    @PostMapping("/{orderNo}/confirm-pickup")
    public Result<Void> confirmPickup(
            @PathVariable String orderNo,
            @RequestParam(required = false) String pickupCode) {
        log.info("确认取货: orderNo={}, pickupCode={}", orderNo, pickupCode);
        productOrderService.confirmPickup(orderNo, pickupCode);
        return Result.success();
    }

    @Operation(summary = "订单发货")
    @PostMapping("/{orderNo}/ship")
    public Result<Void> ship(
            @PathVariable String orderNo,
            @RequestParam String trackingNo,
            @RequestParam String carrier) {
        log.info("订单发货: orderNo={}, trackingNo={}, carrier={}", orderNo, trackingNo, carrier);
        productOrderService.shipOrder(orderNo, trackingNo, carrier);
        return Result.success();
    }

    @Operation(summary = "完成订单")
    @PostMapping("/{orderNo}/complete")
    public Result<Void> complete(@PathVariable String orderNo) {
        log.info("完成订单: orderNo={}", orderNo);
        productOrderService.completeOrder(orderNo);
        return Result.success();
    }
}