package com.fitness.modules.analysis.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI数据分析报告实体类
 */
@Data
@TableName("analysis_report")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AnalysisReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String reportTitle;
    private String analysisType;
    private String reportContent;

    /**
     * 优化建议JSON字符串 - 直接用TEXT存储
     */
    private String suggestionsJson;

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

    // ==================== 非数据库字段 ====================

    /**
     * 优化建议列表（前端显示用）
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @TableField(exist = false)
    private transient List<String> suggestions;

    public List<String> getSuggestions() {
        if (this.suggestions != null && !this.suggestions.isEmpty()) {
            return this.suggestions;
        }
        
        if (this.suggestionsJson != null && !this.suggestionsJson.isEmpty()) {
            return parseJson();
        }
        
        return new ArrayList<>();
    }

    private List<String> parseJson() {
        try {
            if (suggestionsJson.startsWith("[") && suggestionsJson.endsWith("]")) {
                return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(suggestionsJson, 
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            }
            List<String> result = new ArrayList<>();
            result.add(suggestionsJson);
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void setSuggestions(List<String> list) {
        this.suggestions = list;
        if (list != null && !list.isEmpty()) {
            try {
                this.suggestionsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(list);
            } catch (Exception e) {
                this.suggestionsJson = list.toString();
            }
        } else {
            this.suggestionsJson = "[]";
        }
    }
}
