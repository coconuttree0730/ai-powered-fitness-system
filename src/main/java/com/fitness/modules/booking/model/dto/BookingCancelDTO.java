package com.fitness.modules.booking.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 取消预约DTO
 */
@Data
public class BookingCancelDTO {

    /**
     * 取消原因
     */
    @Size(max = 255, message = "取消原因不能超过255个字符")
    private String cancelReason;
}
