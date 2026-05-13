package com.fitness.modules.booking.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建预约DTO（基于课程实例预约）
 */
@Data
public class BookingDTO {

    /**
     * 课程实例ID（必须，用户选择的具体某一天的课程）
     */
    @NotNull(message = "课程实例ID不能为空")
    private Long sessionId;

    /**
     * 课程模板ID（冗余，便于查询）
     */
    private Long courseId;
}
