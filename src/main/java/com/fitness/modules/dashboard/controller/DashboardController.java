package com.fitness.modules.dashboard.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.dashboard.model.dto.AnalysisRequestDTO;
import com.fitness.modules.dashboard.model.vo.AnalysisReportVO;
import com.fitness.modules.dashboard.model.vo.CourseStatsVO;
import com.fitness.modules.dashboard.model.vo.DashboardStatsVO;
import com.fitness.modules.dashboard.model.vo.MemberCardStatsVO;
import com.fitness.modules.dashboard.model.vo.PeakHoursVO;
import com.fitness.modules.dashboard.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘控制器
 * 提供运营数据统计接口
 */
@Slf4j
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
    @GetMapping("/course-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CourseStatsVO>> getCourseStats() {
        log.info("获取课程统计");
        List<CourseStatsVO> courseStats = dashboardService.getCourseStats();
        return Result.success(courseStats);
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
