package com.fitness.modules.analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;

/**
 * AI数据分析报告Service实现类
 */
@Slf4j
@Service
public class AnalysisReportServiceImpl extends ServiceImpl<AnalysisReportMapper, AnalysisReport> implements AnalysisReportService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AnalysisReport saveReport(AnalysisReportVO reportVO, Long userId) {
        log.info("保存AI分析报告，用户ID: {}", userId);
        log.info("报告标题: {}", reportVO.getReportTitle());
        log.info("建议列表: {}", reportVO.getSuggestions());

        AnalysisReport report = new AnalysisReport();
        report.setReportTitle(reportVO.getReportTitle());
        report.setAnalysisType(reportVO.getAnalysisType());
        report.setReportContent(reportVO.getReportContent());
        
        // 确保suggestions是正确的格式
        if (reportVO.getSuggestions() != null) {
            report.setSuggestions(reportVO.getSuggestions());
            log.info("设置的建议列表大小: {}", reportVO.getSuggestions().size());
        }
        
        report.setGenerateTime(reportVO.getGenerateTime() != null ?
                reportVO.getGenerateTime() : LocalDateTime.now());
        report.setCreateBy(userId);
        report.setIsDeleted(false);

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

        IPage<AnalysisReport> resultPage = this.page(pageParam, wrapper);

        // 处理每条记录的suggestions字段
        for (AnalysisReport record : resultPage.getRecords()) {
            processSuggestions(record);
        }

        return resultPage;
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

        // 处理suggestions字段
        processSuggestions(report);

        return report;
    }

    /**
     * 处理suggestions字段，确保它是List<String>类型
     * 从数据库的JSON字符串解析为List
     */
    private void processSuggestions(AnalysisReport report) {
        if (report == null) {
            return;
        }

        // 获取数据库存储的JSON字符串
        String suggestionsJson = report.getSuggestionsJson();
        log.info("processSuggestions - suggestionsJson: {}", 
                suggestionsJson != null ? suggestionsJson.substring(0, Math.min(100, suggestionsJson.length())) + "..." : "null");

        if (suggestionsJson != null && !suggestionsJson.isEmpty()) {
            List<String> processedSuggestions = null;

            try {
                if (suggestionsJson.startsWith("[") && suggestionsJson.endsWith("]")) {
                    // 是JSON数组格式，解析为List
                    processedSuggestions = objectMapper.readValue(suggestionsJson, new TypeReference<List<String>>() {});
                    log.info("解析后的suggestions大小: {}", processedSuggestions.size());
                } else {
                    // 单个建议字符串
                    processedSuggestions = new ArrayList<>();
                    processedSuggestions.add(suggestionsJson);
                }
                
                report.setSuggestions(processedSuggestions);
                log.info("最终设置的suggestions大小: {}", processedSuggestions.size());
                
            } catch (Exception e) {
                log.error("处理suggestions字段失败", e);
                report.setSuggestions(new ArrayList<>());
            }
        } else {
            log.warn("suggestionsJson为空");
            report.setSuggestions(new ArrayList<>());
        }
    }
}
