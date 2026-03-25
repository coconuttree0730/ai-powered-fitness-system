package com.fitness.modules.booking.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建预约DTO
 */
@Data
public class BookingDTO {

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
}
