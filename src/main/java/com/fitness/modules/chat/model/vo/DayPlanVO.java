package com.fitness.modules.chat.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 日程计划VO
 * 每天的训练安排
 */
@Data
public class DayPlanVO {

    /**
     * 星期几(1-7)
     */
    private Integer dayOfWeek;

    /**
     * 星期名称(周一、周二...)
     */
    private String dayName;

    /**
     * 训练类型(力量训练/有氧训练/休息恢复等)
     */
    private String trainingType;

    /**
     * 训练重点(胸部、背部、腿部等)
     */
    private String focus;

    /**
     * 训练总时长(分钟)
     */
    private Integer totalDuration;

    /**
     * 训练动作列表
     */
    private List<ExerciseVO> exercises;

    /**
     * 每日建议/注意事项
     */
    private String dailyTips;
}
