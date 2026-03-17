package com.fitness.modules.course.model.enums;

import lombok.Getter;

/**
 * 课程分类枚举
 */
@Getter
public enum CourseCategory {

    YOGA("瑜伽"),
    PILATES("普拉提"),
    HIIT("高强度间歇训练"),
    STRENGTH("力量训练"),
    CARDIO("有氧训练"),
    DANCE("舞蹈");

    private final String description;

    CourseCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
