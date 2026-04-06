package com.fitness.modules.chat.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 健身计划卡片VO
 * AI生成的健身计划展示数据
 */
@Data
public class FitnessPlanCardVO {

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 健身目标
     */
    private String goal;

    /**
     * 训练部位
     */
    private String bodyPart;

    /**
     * 经验水平
     */
    private String experience;

    /**
     * 计划周期(天)
     */
    private Integer duration;

    /**
     * 每周训练安排
     */
    private List<DayPlanVO> weeklyPlan;

    /**
     * 每周建议
     */
    private WeeklyAdviceVO weeklyAdvice;

    /**
     * 推荐课程
     */
    private List<CourseRecommendVO> recommendedCourses;

    /**
     * 推荐器械
     */
    private List<EquipmentRecommendVO> recommendedEquipment;
}
