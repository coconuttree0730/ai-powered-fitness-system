package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.product.model.dto.CalculatePriceDTO;
import com.fitness.modules.product.model.dto.ProductOrderDTO;
import com.fitness.modules.product.model.vo.PriceCalculationVO;
import com.fitness.modules.product.model.vo.ProductOrderVO;
import com.fitness.modules.product.service.ProductOrderService;
import com.fitness.integration.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品订单", description = "商品订单相关接口")
@RestController
@RequestMapping("/api/v1/product-orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProductOrderController {
    
    private final ProductOrderService productOrderService;
    
    @Operation(summary = "计算订单价格")
    @PostMapping("/calculate")
    public Result<PriceCalculationVO> calculatePrice(@Valid @RequestBody CalculatePriceDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(productOrderService.calculatePrice(dto, userId));
    }
    
    @Operation(summary = "创建订单")
    @PostMapping
    public Result<ProductOrderVO> createOrder(@Valid @RequestBody ProductOrderDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(productOrderService.createOrder(dto, userId));
    }
    
    @Operation(summary = "获取用户订单列表")
    @GetMapping
    public Result<List<ProductOrderVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(productOrderService.getUserOrders(userId));
    }
    
    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderNo}")
    public Result<ProductOrderVO> detail(@PathVariable String orderNo) {
        return Result.success(productOrderService.getOrderDetail(orderNo));
    }
    
    @Operation(summary = "支付订单")
    @PostMapping("/{orderNo}/pay")
    public Result<ProductOrderVO> pay(@PathVariable String orderNo, @RequestParam String payMethod) {
        return Result.success(productOrderService.payOrder(orderNo, payMethod));
    }
    
    @Operation(summary = "取消订单")
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        productOrderService.cancelOrder(orderNo);
        return Result.success();
    }
}
