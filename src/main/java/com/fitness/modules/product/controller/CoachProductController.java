package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.coach.model.dto.CoachPackageDTO;
import com.fitness.modules.coach.model.vo.CoachPackageVO;
import com.fitness.modules.coach.service.CoachPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教练套餐", description = "教练套餐管理接口（已迁移至 CoachPackage，保留兼容路径）")
@RestController
@RequestMapping("/api/v1/coach/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class CoachProductController {

    private final CoachPackageService coachPackageService;

    @Operation(summary = "获取我的套餐列表")
    @GetMapping
    public Result<List<CoachPackageVO>> listMyProducts() {
        Long coachId = SecurityUtils.getCurrentUserId();
        return Result.success(coachPackageService.getCoachPackages(coachId));
    }

    @Operation(summary = "创建套餐")
    @PostMapping
    public Result<CoachPackageVO> create(@Valid @RequestBody CoachPackageDTO dto) {
        Long coachId = SecurityUtils.getCurrentUserId();
        return Result.success(coachPackageService.createPackage(dto, coachId));
    }

    @Operation(summary = "更新套餐")
    @PutMapping("/{id}")
    public Result<CoachPackageVO> update(@PathVariable Long id, @Valid @RequestBody CoachPackageDTO dto) {
        Long coachId = SecurityUtils.getCurrentUserId();
        return Result.success(coachPackageService.updatePackage(id, dto, coachId));
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long coachId = SecurityUtils.getCurrentUserId();
        coachPackageService.deletePackage(id, coachId);
        return Result.success();
    }

    @Operation(summary = "更新套餐状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Long coachId = SecurityUtils.getCurrentUserId();
        coachPackageService.updateStatus(id, status, coachId);
        return Result.success();
    }
}