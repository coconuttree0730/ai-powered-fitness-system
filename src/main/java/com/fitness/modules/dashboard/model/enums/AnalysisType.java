package com.fitness.modules.dashboard.model.enums;

import lombok.Getter;

/**
 * 分析类型枚举
 */
@Getter
public enum AnalysisType {

    MEMBER("MEMBER", "会员分析"),
    COURSE("COURSE", "课程分析"),
    EQUIPMENT("EQUIPMENT", "器材分析"),
    OVERALL("OVERALL", "综合分析");

    private final String code;
    private final String description;

    AnalysisType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AnalysisType fromCode(String code) {
        for (AnalysisType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OVERALL;
    }
}
