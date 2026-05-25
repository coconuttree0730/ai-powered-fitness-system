package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.product.model.dto.ProductDTO;
import com.fitness.modules.product.model.vo.ProductVO;
import com.fitness.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教练商品", description = "教练商品管理接口")
@RestController
@RequestMapping("/api/v1/coach/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class CoachProductController {

    private final ProductService productService;

    @Operation(summary = "获取我的商品列表")
    @GetMapping
    public Result<List<ProductVO>> listMyProducts() {
        Long coachId = SecurityUtils.getCurrentUserId();
        return Result.success(productService.getProductsByCoachId(coachId));
    }

    @Operation(summary = "创建商品")
    @PostMapping
    public Result<ProductVO> create(@Valid @RequestBody ProductDTO dto) {
        Long coachId = SecurityUtils.getCurrentUserId();
        dto.setCoachId(coachId);
        dto.setCategory("COURSE");
        return Result.success(productService.createProduct(dto));
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        Long coachId = SecurityUtils.getCurrentUserId();
        dto.setId(id);
        dto.setCoachId(coachId);
        return Result.success(productService.updateProduct(dto));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long coachId = SecurityUtils.getCurrentUserId();
        productService.deleteCoachProduct(id, coachId);
        return Result.success();
    }

    @Operation(summary = "更新商品状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Long coachId = SecurityUtils.getCurrentUserId();
        productService.updateCoachProductStatus(id, coachId, status);
        return Result.success();
    }
}