package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.product.model.dto.ProductDTO;
import com.fitness.modules.product.model.dto.StockUpdateDTO;
import com.fitness.modules.product.model.vo.ProductVO;
import com.fitness.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<ProductVO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        log.info("获取商品列表, category: {}, status: {}, keyword: {}", category, status, keyword);
        return Result.success(productService.getAllProducts(category, status, keyword));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ProductVO> create(@Valid @RequestBody ProductDTO dto) {
        log.info("创建商品: {}", dto.getName());
        return Result.success(productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        log.info("更新商品: id={}", id);
        dto.setId(id);
        return Result.success(productService.updateProduct(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除商品: id={}", id);
        productService.deleteProduct(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新商品状态: id={}, status={}", id, status);
        productService.updateProductStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ProductVO> updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateDTO dto) {
        log.info("更新商品库存: id={}, type={}, quantity={}", id, dto.getType(), dto.getQuantity());
        return Result.success(productService.updateStock(id, dto));
    }
}
