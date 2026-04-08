package com.fitness.modules.analysis.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI数据分析报告实体类
 */
@Data
@TableName("analysis_report")
public class AnalysisReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报告标题
     */
    private String reportTitle;

    /**
     * 分析类型：OVERALL-综合分析, MEMBER-会员分析, COURSE-课程分析, EQUIPMENT-器材分析, REVENUE-营收分析
     */
    private String analysisType;

    /**
     * LLM返回的Markdown格式报告内容
     */
    private String reportContent;

    /**
     * 优化建议列表（JSON数组格式）
     */
    @TableField(typeHandler = com.fitness.common.mybatis.JsonbTypeHandler.class)
    private List<String> suggestions;

    /**
     * 报告生成时间
     */
    private LocalDateTime generateTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID（管理员ID）
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean isDeleted;
}
