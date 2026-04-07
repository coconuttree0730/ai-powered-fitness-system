package com.fitness.modules.dashboard.service;

import com.fitness.modules.dashboard.model.vo.AnalysisReportVO;
import com.fitness.modules.dashboard.model.vo.CourseStatsVO;
import com.fitness.modules.dashboard.model.vo.DashboardStatsVO;
import com.fitness.modules.dashboard.model.vo.EquipmentStatusVO;
import com.fitness.modules.dashboard.model.vo.MemberCardStatsVO;
import com.fitness.modules.dashboard.model.vo.PeakHoursVO;
import com.fitness.modules.dashboard.model.vo.RepairStatsVO;
import com.fitness.modules.dashboard.model.vo.RevenueTrendVO;
import com.fitness.modules.dashboard.model.vo.UserGrowthVO;

import java.util.List;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     *
     * @return 统计数据
     */
    DashboardStatsVO getDashboardStats();

    /**
     * 获取会员卡销量统计
     * MVP阶段使用模拟数据
     *
     * @return 会员卡销量统计
     */
    MemberCardStatsVO getMemberCardStats();

    /**
     * 获取到店高峰时间
     *
     * @return 高峰时间列表
     */
    List<PeakHoursVO> getPeakHours();

    /**
     * 获取课程统计
     *
     * @return 课程统计列表
     */
    List<CourseStatsVO> getCourseStats();

    /**
     * 生成AI分析报告
     *
     * @param analysisType 分析类型：MEMBER, COURSE, EQUIPMENT, OVERALL
     * @return 分析报告
     */
    AnalysisReportVO generateAnalysisReport(String analysisType);

    /**
     * 获取营收趋势数据
     *
     * @param range 时间范围：today/week/month/year
     * @return 营收趋势数据列表
     */
    List<RevenueTrendVO> getRevenueTrend(String range);

    /**
     * 获取用户增长趋势
     *
     * @param range 时间范围：today/week/month/year
     * @return 用户增长数据列表
     */
    List<UserGrowthVO> getUserGrowth(String range);

    /**
     * 获取器材使用状态统计
     *
     * @return 器材状态数据
     */
    EquipmentStatusVO getEquipmentStatus();

    /**
     * 获取报修处理统计
     *
     * @return 报修统计数据列表
     */
    List<RepairStatsVO> getRepairStats();
}
