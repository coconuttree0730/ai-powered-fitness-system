package com.fitness.modules.plan.model.enums;

import lombok.Getter;

/**
 * 健身目标枚举
 */
@Getter
public enum FitnessGoal {

    MUSCLE_GAIN("增肌"),
    FAT_LOSS("减脂"),
    BODY_SHAPING("塑形"),
    ENDURANCE("耐力提升");

    private final String description;

    FitnessGoal(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
