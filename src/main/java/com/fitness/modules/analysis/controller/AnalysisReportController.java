package com.fitness.modules.analysis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.analysis.model.entity.AnalysisReport;
import com.fitness.modules.analysis.service.AnalysisReportService;
import com.fitness.modules.dashboard.model.vo.AnalysisReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI数据分析报告Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis-reports")
@RequiredArgsConstructor
@Tag(name = "AI数据分析报告管理", description = "AI数据分析报告的保存、查询、删除等操作")
public class AnalysisReportController {

    private final AnalysisReportService analysisReportService;

    /**
     * 保存AI分析报告
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "保存AI分析报告", description = "将AI生成的分析报告保存到数据库")
    public Result<AnalysisReport> saveReport(@RequestBody AnalysisReportVO reportVO) {
        Long userId = SecurityUtils.requireCurrentUserId();
        AnalysisReport report = analysisReportService.saveReport(reportVO, userId);
        return Result.success(report);
    }

    /**
     * 分页查询报告列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询报告列表", description = "分页查询AI分析报告列表，支持按类型和时间筛选")
    public Result<IPage<AnalysisReport>> getReportPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String timeFilter) {
        Long userId = SecurityUtils.requireCurrentUserId();
        IPage<AnalysisReport> reportPage = analysisReportService.getReportPage(page, size, userId, type, timeFilter);
        return Result.success(reportPage);
    }

    /**
     * 获取报告详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取报告详情", description = "根据ID获取AI分析报告详情")
    public Result<AnalysisReport> getReportDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        AnalysisReport report = analysisReportService.getReportDetail(id, userId);
        return Result.success(report);
    }

    /**
     * 删除报告
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除报告", description = "根据ID删除AI分析报告")
    public Result<Void> deleteReport(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        boolean success = analysisReportService.deleteReport(id, userId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 批量删除报告
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量删除报告", description = "批量删除AI分析报告")
    public Result<Void> batchDeleteReports(@RequestBody List<Long> ids) {
        boolean success = analysisReportService.removeByIds(ids);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }
}