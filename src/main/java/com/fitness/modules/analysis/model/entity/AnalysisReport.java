package com.fitness.modules.analysis.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI数据分析报告实体类
 */
@Data
@TableName("analysis_report")
public class AnalysisReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String reportTitle;
    private String analysisType;
    private String reportContent;

    /**
     * 优化建议 - Markdown格式文本（TEXT存储）
     */
    private String suggestions;

    private LocalDateTime generateTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean isDeleted;
}
