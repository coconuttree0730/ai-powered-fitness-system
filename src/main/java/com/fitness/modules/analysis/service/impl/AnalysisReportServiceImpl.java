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

/**
 * AI数据分析报告Service实现类
 */
@Slf4j
@Service
public class AnalysisReportServiceImpl extends ServiceImpl<AnalysisReportMapper, AnalysisReport> implements AnalysisReportService {

    @Override
    public AnalysisReport saveReport(AnalysisReportVO reportVO, Long userId) {
        log.info("保存AI分析报告，用户ID: {}", userId);
        log.info("报告标题: {}", reportVO.getReportTitle());

        AnalysisReport report = new AnalysisReport();
        report.setReportTitle(reportVO.getReportTitle());
        report.setAnalysisType(reportVO.getAnalysisType());
        report.setReportContent(reportVO.getReportContent());
        
        // 将List<String>转换为Markdown格式文本
        if (reportVO.getSuggestions() != null && !reportVO.getSuggestions().isEmpty()) {
            String markdownSuggestions = convertToMarkdown(reportVO.getSuggestions());
            report.setSuggestions(markdownSuggestions);
            log.info("设置的建议内容长度: {}", markdownSuggestions.length());
        }
        
        report.setGenerateTime(reportVO.getGenerateTime() != null ?
                reportVO.getGenerateTime() : LocalDateTime.now());
        report.setCreateBy(userId);
        report.setIsDeleted(false);

        this.save(report);
        log.info("AI分析报告保存成功，ID: {}", report.getId());
        return report;
    }

    /**
     * 将建议列表转换为Markdown格式
     */
    private String convertToMarkdown(java.util.List<String> suggestions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < suggestions.size(); i++) {
            sb.append((i + 1) + ". " + suggestions.get(i) + "\n\n");
        }
        return sb.toString();
    }

    @Override
    public IPage<AnalysisReport> getReportPage(int page, int size, Long userId, String type, String timeFilter) {
        log.info("查询报告列表，页码: {}, 每页大小: {}, 用户ID: {}, 类型: {}, 时间筛选: {}",
                page, size, userId, type, timeFilter);

        Page<AnalysisReport> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AnalysisReport> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(AnalysisReport::getCreateBy, userId);

        if (type != null && !type.isEmpty()) {
            wrapper.eq(AnalysisReport::getAnalysisType, type);
        }

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

        if (!report.getCreateBy().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return report;
    }
}
