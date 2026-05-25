package com.fitness.modules.chat.model.vo;

import lombok.Data;

/**
 * 训练动作VO
 * 单个训练动作的详细信息
 */
@Data
public class ExerciseVO {

    /**
     * 动作名称
     */
    private String name;

    /**
     * 动作描述
     */
    private String description;

    /**
     * 目标肌群
     */
    private String targetMuscle;

    /**
     * 组数
     */
    private Integer sets;

    /**
     * 每组次数
     */
    private Integer reps;

    /**
     * 持续时间(分钟)，用于有氧运动
     */
    private Integer duration;

    /**
     * 休息时间(秒)
     */
    private Integer restTime;

    /**
     * 动作要点/注意事项
     */
    private String tips;

    /**
     * 难度等级：EASY-简单, MEDIUM-中等, HARD-困难
     */
    private String difficulty;
}
