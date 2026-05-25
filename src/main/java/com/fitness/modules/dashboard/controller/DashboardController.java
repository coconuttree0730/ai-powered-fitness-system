package com.fitness.modules.dashboard.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.dashboard.model.dto.AnalysisRequestDTO;
import com.fitness.modules.dashboard.model.vo.AnalysisReportVO;
import com.fitness.modules.dashboard.model.vo.CourseStatsVO;
import com.fitness.modules.dashboard.model.vo.DashboardStatsVO;
import com.fitness.modules.dashboard.model.vo.EquipmentStatusVO;
import com.fitness.modules.dashboard.model.vo.MemberCardStatsVO;
import com.fitness.modules.dashboard.model.vo.PeakHoursVO;
import com.fitness.modules.dashboard.model.vo.RepairStatsVO;
import com.fitness.modules.dashboard.model.vo.RevenueTrendVO;
import com.fitness.modules.dashboard.model.vo.UserGrowthVO;
import com.fitness.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘控制器
 * 提供运营数据统计接口
 */
@Slf4j
@Tag(name = "数据仪表盘", description = "运营数据统计与AI分析报告接口")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     *
     * @return 统计数据
     */
    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DashboardStatsVO> getDashboardStats() {
        log.info("获取仪表盘统计数据");
        DashboardStatsVO stats = dashboardService.getDashboardStats();
        return Result.success(stats);
    }

    /**
     * 获取会员卡销量统计
     *
     * @return 会员卡销量统计
     */
    @Operation(summary = "获取会员卡销量统计")
    @GetMapping("/member-cards")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<MemberCardStatsVO> getMemberCardStats() {
        log.info("获取会员卡销量统计");
        MemberCardStatsVO stats = dashboardService.getMemberCardStats();
        return Result.success(stats);
    }

    /**
     * 获取到店高峰时间
     *
     * @return 高峰时间列表
     */
    @Operation(summary = "获取到店高峰时间")
    @GetMapping("/peak-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<PeakHoursVO>> getPeakHours() {
        log.info("获取到店高峰时间");
        List<PeakHoursVO> peakHours = dashboardService.getPeakHours();
        return Result.success(peakHours);
    }

    /**
     * 获取课程统计
     *
     * @return 课程统计列表
     */
    @Operation(summary = "获取课程统计")
    @GetMapping("/course-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CourseStatsVO>> getCourseStats() {
        log.info("获取课程统计");
        List<CourseStatsVO> courseStats = dashboardService.getCourseStats();
        return Result.success(courseStats);
    }

    /**
     * 获取营收趋势数据
     *
     * @param range 时间范围：today/week/month/year
     * @return 营收趋势数据
     */
    @Operation(summary = "获取营收趋势数据")
    @GetMapping("/revenue-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RevenueTrendVO>> getRevenueTrend(
            @RequestParam(defaultValue = "week") String range) {
        log.info("获取营收趋势数据，时间范围: {}", range);
        List<RevenueTrendVO> data = dashboardService.getRevenueTrend(range);
        return Result.success(data);
    }

    /**
     * 获取用户增长趋势
     *
     * @param range 时间范围：today/week/month/year
     * @return 用户增长数据
     */
    @Operation(summary = "获取用户增长趋势")
    @GetMapping("/user-growth")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserGrowthVO>> getUserGrowth(
            @RequestParam(defaultValue = "week") String range) {
        log.info("获取用户增长趋势，时间范围: {}", range);
        List<UserGrowthVO> data = dashboardService.getUserGrowth(range);
        return Result.success(data);
    }

    /**
     * 获取器材使用状态统计
     *
     * @return 器材状态数据
     */
    @Operation(summary = "获取器材使用状态统计")
    @GetMapping("/equipment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<EquipmentStatusVO> getEquipmentStatus() {
        log.info("获取器材使用状态统计");
        EquipmentStatusVO status = dashboardService.getEquipmentStatus();
        return Result.success(status);
    }

    /**
     * 获取报修处理统计
     *
     * @return 报修统计数据
     */
    @Operation(summary = "获取报修处理统计")
    @GetMapping("/repair-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RepairStatsVO>> getRepairStats() {
        log.info("获取报修处理统计");
        List<RepairStatsVO> stats = dashboardService.getRepairStats();
        return Result.success(stats);
    }

    /**
     * 生成AI分析报告
     *
     * @param dto 分析请求参数
     * @return 分析报告
     */
    @PostMapping("/analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AnalysisReportVO> generateAnalysisReport(@Valid @RequestBody AnalysisRequestDTO dto) {
        log.info("生成AI分析报告请求，分析类型: {}", dto.getAnalysisType());
        AnalysisReportVO report = dashboardService.generateAnalysisReport(dto.getAnalysisType());
        return Result.success(report);
    }
}
