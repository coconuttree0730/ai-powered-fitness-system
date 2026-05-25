package com.fitness.modules.dashboard.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI分析报告VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReportVO {

    /**
     * 分析类型
     */
    private String analysisType;

    /**
     * 报告标题
     */
    private String reportTitle;

    /**
     * 报告内容
     */
    private String reportContent;

    /**
     * 建议列表
     */
    private List<String> suggestions;

    /**
     * 生成时间
     */
    private LocalDateTime generateTime;
}
