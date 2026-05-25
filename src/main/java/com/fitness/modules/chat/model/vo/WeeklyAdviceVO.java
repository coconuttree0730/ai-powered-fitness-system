package com.fitness.modules.chat.model.vo;

import lombok.Data;

/**
 * 每周建议VO
 * 健身计划的总体建议
 */
@Data
public class WeeklyAdviceVO {

    /**
     * 总体目标
     */
    private String overallGoal;

    /**
     * 训练原则
     */
    private String trainingPrinciples;

    /**
     * 饮食建议
     */
    private String dietAdvice;

    /**
     * 休息恢复建议
     */
    private String restAdvice;

    /**
     * 安全注意事项
     */
    private String safetyTips;

    /**
     * 进阶建议
     */
    private String progressionTips;
}
