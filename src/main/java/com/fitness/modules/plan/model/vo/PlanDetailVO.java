package com.fitness.modules.plan.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 计划详情VO
 */
@Data
public class PlanDetailVO {

    /**
     * 详情ID
     */
    private Long id;

    /**
     * 天数索引(1-7)
     */
    private Integer dayIndex;

    /**
     * 星期名称(周一、周二等)
     */
    private String dayName;

    /**
     * 训练重点(胸部、背部等)
     */
    private String focus;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程封面图
     */
    private String courseCoverImage;

    /**
     * 课程描述
     */
    private String courseDescription;

    /**
     * 器械信息列表
     */
    private List<Map<String, Object>> equipment;

    /**
     * 训练动作列表
     */
    private List<Map<String, Object>> exercises;

    /**
     * 持续时间(分钟)
     */
    private Integer duration;

    /**
     * 备注
     */
    private String notes;
}
