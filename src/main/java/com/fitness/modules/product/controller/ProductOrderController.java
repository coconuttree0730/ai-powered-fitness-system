package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.product.model.dto.CalculatePriceDTO;
import com.fitness.modules.product.model.dto.ProductOrderDTO;
import com.fitness.modules.product.model.vo.PriceCalculationVO;
import com.fitness.modules.product.model.vo.ProductOrderVO;
import com.fitness.modules.product.service.ProductOrderService;
import com.fitness.integration.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-orders")
@RequiredArgsConstructor
public class ProductOrderController {
    
    private final ProductOrderService productOrderService;
    
    @PostMapping("/calculate")
    public Result<PriceCalculationVO> calculatePrice(@Valid @RequestBody CalculatePriceDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(productOrderService.calculatePrice(dto, userId));
    }
    
    @PostMapping
    public Result<ProductOrderVO> createOrder(@Valid @RequestBody ProductOrderDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(productOrderService.createOrder(dto, userId));
    }
    
    @GetMapping
    public Result<List<ProductOrderVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(productOrderService.getUserOrders(userId));
    }
    
    @GetMapping("/{orderNo}")
    public Result<ProductOrderVO> detail(@PathVariable String orderNo) {
        return Result.success(productOrderService.getOrderDetail(orderNo));
    }
    
    @PostMapping("/{orderNo}/pay")
    public Result<ProductOrderVO> pay(@PathVariable String orderNo, @RequestParam String payMethod) {
        return Result.success(productOrderService.payOrder(orderNo, payMethod));
    }
    
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        productOrderService.cancelOrder(orderNo);
        return Result.success();
    }
}
