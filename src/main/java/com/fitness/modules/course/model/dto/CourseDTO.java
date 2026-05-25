package com.fitness.modules.course.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

/**
 * 课程创建/更新请求DTO
 * 业务场景：公开课是周期性课程（每周固定某天某时段）
 */
@Data
public class CourseDTO {

    /**
     * 课程名称
     */
    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 教练ID
     */
    @NotNull(message = "教练ID不能为空")
    private Long coachId;

    /**
     * 课程分类
     */
    @NotBlank(message = "课程分类不能为空")
    private String category;

    /**
     * 星期几：1-周一, 2-周二, 3-周三, 4-周四, 5-周五, 6-周六, 7-周日
     */
    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几必须在1-7之间")
    @Max(value = 7, message = "星期几必须在1-7之间")
    private Integer dayOfWeek;

    /**
     * 开始时间（时分秒，如 14:00:00）
     */
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    /**
     * 结束时间（时分秒，如 15:30:00）
     */
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    /**
     * 容量（可预约人数）
     */
    @NotNull(message = "容量不能为空")
    private Integer capacity;

    /**
     * 状态：0-未开始, 1-进行中, 2-已结束, 3-已取消
     */
    private Integer status;

    /**
     * 课程图片URL
     */
    private String imageUrl;

    /**
     * 难度等级：入门/初级/中级/高级/进阶
     */
    private String difficultyLevel;

    /**
     * 课程时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 最小卡路里消耗
     */
    private Integer caloriesMin;

    /**
     * 最大卡路里消耗
     */
    private Integer caloriesMax;
}
