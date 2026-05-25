package com.fitness.modules.analysis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.analysis.model.entity.AnalysisReport;
import com.fitness.modules.dashboard.model.vo.AnalysisReportVO;


/**
 * AI数据分析报告Service接口
 */
public interface AnalysisReportService extends IService<AnalysisReport> {

    /**
     * 保存AI分析报告
     *
     * @param reportVO 报告VO
     * @param userId   用户ID
     * @return 保存后的报告实体
     */
    AnalysisReport saveReport(AnalysisReportVO reportVO, Long userId);

    /**
     * 分页查询报告列表
     *
     * @param page     页码
     * @param size     每页大小
     * @param userId   用户ID
     * @param type     分析类型（可选）
     * @param timeFilter 时间筛选：today-本日, month-本月, year-本年
     * @return 分页结果
     */
    IPage<AnalysisReport> getReportPage(int page, int size, Long userId, String type, String timeFilter);

    /**
     * 删除报告
     *
     * @param id     报告ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteReport(Long id, Long userId);

    /**
     * 获取报告详情
     *
     * @param id     报告ID
     * @param userId 用户ID
     * @return 报告实体
     */
    AnalysisReport getReportDetail(Long id, Long userId);
}
