package com.fitness.modules.knowledge.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ARCHIVED(2, "已归档");

    private final Integer code;
    private final String description;

    public static DocumentStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DocumentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
