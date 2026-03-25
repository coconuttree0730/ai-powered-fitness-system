package com.fitness.modules.booking.model.dto;

import lombok.Data;

/**
 * 预约查询DTO
 */
@Data
public class BookingQueryDTO {

    /**
     * 用户ID（管理端查询用）
     */
    private Long userId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 状态：0-待确认，1-已确认，2-已取消，3-已完成
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
