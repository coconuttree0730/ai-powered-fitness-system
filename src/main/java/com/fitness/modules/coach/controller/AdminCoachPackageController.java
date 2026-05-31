package com.fitness.modules.coach.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.modules.coach.model.dto.CoachPackageDTO;
import com.fitness.modules.coach.model.vo.CoachPackageVO;
import com.fitness.modules.coach.service.CoachPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "私教套餐管理（后台）", description = "后台私教套餐CRUD接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/coach-packages")
@RequiredArgsConstructor
public class AdminCoachPackageController {

    private final CoachPackageService coachPackageService;

    @Operation(summary = "分页查询套餐列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<CoachPackageVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long coachId) {
        log.info("管理端查询私教套餐列表: page={}, pageSize={}, keyword={}, status={}, coachId={}", page, pageSize, keyword, status, coachId);
        return Result.success(coachPackageService.getAdminPage(page, pageSize, keyword, status, coachId));
    }

    @Operation(summary = "创建套餐")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CoachPackageVO> create(@Valid @RequestBody CoachPackageDTO dto,
                                          @RequestParam Long coachId) {
        log.info("管理端创建私教套餐: coachId={}, name={}", coachId, dto.getName());
        return Result.success(coachPackageService.createPackageAsAdmin(dto, coachId));
    }

    @Operation(summary = "编辑套餐")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CoachPackageVO> update(@PathVariable Long id, @Valid @RequestBody CoachPackageDTO dto) {
        log.info("管理端更新私教套餐: id={}, name={}", id, dto.getName());
        return Result.success(coachPackageService.updatePackageAsAdmin(id, dto));
    }

    @Operation(summary = "更新套餐状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("管理端更新套餐状态: id={}, status={}", id, status);
        coachPackageService.updateStatusAsAdmin(id, status);
        return Result.success();
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("管理端删除套餐: id={}", id);
        coachPackageService.deletePackageAsAdmin(id);
        return Result.success();
    }
}