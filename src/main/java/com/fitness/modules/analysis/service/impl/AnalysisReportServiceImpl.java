package com.fitness.modules.analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.analysis.mapper.AnalysisReportMapper;
import com.fitness.modules.analysis.model.entity.AnalysisReport;
import com.fitness.modules.analysis.service.AnalysisReportService;
import com.fitness.modules.dashboard.model.vo.AnalysisReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * AI数据分析报告Service实现类
 */
@Slf4j
@Service
public class AnalysisReportServiceImpl extends ServiceImpl<AnalysisReportMapper, AnalysisReport> implements AnalysisReportService {

    @Override
    public AnalysisReport saveReport(AnalysisReportVO reportVO, Long userId) {
        log.info("保存AI分析报告，用户ID: {}", userId);

        AnalysisReport report = new AnalysisReport();
        report.setReportTitle(reportVO.getReportTitle());
        report.setAnalysisType(reportVO.getAnalysisType());
        report.setReportContent(reportVO.getReportContent());
        report.setSuggestions(reportVO.getSuggestions());
        report.setGenerateTime(reportVO.getGenerateTime() != null ?
                reportVO.getGenerateTime() : LocalDateTime.now());
        report.setCreateBy(userId);
        report.setIsDeleted(false);  // 设置默认值

        this.save(report);
        log.info("AI分析报告保存成功，ID: {}", report.getId());
        return report;
    }

    @Override
    public IPage<AnalysisReport> getReportPage(int page, int size, Long userId, String type, String timeFilter) {
        log.info("查询报告列表，页码: {}, 每页大小: {}, 用户ID: {}, 类型: {}, 时间筛选: {}",
                page, size, userId, type, timeFilter);

        Page<AnalysisReport> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AnalysisReport> wrapper = new LambdaQueryWrapper<>();

        // 只查询当前用户的报告
        wrapper.eq(AnalysisReport::getCreateBy, userId);

        // 按类型筛选
        if (type != null && !type.isEmpty()) {
            wrapper.eq(AnalysisReport::getAnalysisType, type);
        }

        // 按时间筛选
        if (timeFilter != null && !timeFilter.isEmpty()) {
            LocalDateTime startTime;
            LocalDateTime endTime = LocalDateTime.now();

            switch (timeFilter) {
                case "today":
                    startTime = LocalDate.now().atStartOfDay();
                    break;
                case "month":
                    startTime = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                    break;
                case "year":
                    startTime = LocalDate.now().withDayOfYear(1).atStartOfDay();
                    break;
                default:
                    startTime = null;
            }

            if (startTime != null) {
                wrapper.between(AnalysisReport::getGenerateTime, startTime, endTime);
            }
        }

        // 按生成时间倒序排列
        wrapper.orderByDesc(AnalysisReport::getGenerateTime);

        return this.page(pageParam, wrapper);
    }

    @Override
    public boolean deleteReport(Long id, Long userId) {
        log.info("删除AI分析报告，ID: {}, 用户ID: {}", id, userId);

        AnalysisReport report = this.getById(id);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 只能删除自己的报告
        if (!report.getCreateBy().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return this.removeById(id);
    }

    @Override
    public AnalysisReport getReportDetail(Long id, Long userId) {
        log.info("获取报告详情，ID: {}, 用户ID: {}", id, userId);

        AnalysisReport report = this.getById(id);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 只能查看自己的报告
        if (!report.getCreateBy().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return report;
    }
}
