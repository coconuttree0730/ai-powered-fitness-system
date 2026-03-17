package com.fitness.modules.dashboard.service;

import com.fitness.modules.dashboard.model.vo.AnalysisReportVO;
import com.fitness.modules.dashboard.model.vo.CourseStatsVO;
import com.fitness.modules.dashboard.model.vo.DashboardStatsVO;
import com.fitness.modules.dashboard.model.vo.MemberCardStatsVO;
import com.fitness.modules.dashboard.model.vo.PeakHoursVO;

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
}
