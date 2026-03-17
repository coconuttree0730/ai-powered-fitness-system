package com.fitness.modules.dashboard.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI分析请求DTO
 */
@Data
public class AnalysisRequestDTO {

    /**
     * 分析类型：MEMBER, COURSE, EQUIPMENT, OVERALL
     */
    @NotBlank(message = "分析类型不能为空")
    private String analysisType;
}
