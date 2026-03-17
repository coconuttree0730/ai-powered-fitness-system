package com.fitness.modules.course.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程创建/更新请求DTO
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
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是未来时间")
    private LocalDateTime endTime;

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
}
