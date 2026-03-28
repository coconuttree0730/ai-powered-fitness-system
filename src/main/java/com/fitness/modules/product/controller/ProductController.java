package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.product.model.vo.ProductVO;
import com.fitness.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public Result<List<ProductVO>> list(@RequestParam(required = false) String category) {
        return Result.success(productService.getProductList(category));
    }
    
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }
}
