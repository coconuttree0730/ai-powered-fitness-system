package com.fitness.modules.booking.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约列表VO
 */
@Data
public class BookingListVO {

    /**
     * 预约ID
     */
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教练名称
     */
    private String coachName;

    /**
     * 星期几：1-周一, 2-周二, ..., 7-周日
     */
    private Integer dayOfWeek;

    /**
     * 课程开始时间（时分秒，如 14:00:00）
     */
    private LocalTime startTime;

    /**
     * 课程结束时间（时分秒，如 15:30:00）
     */
    private LocalTime endTime;

    /**
     * 预约时间
     */
    private LocalDateTime bookingTime;

    /**
     * 状态：0-待确认，1-已确认，2-已取消，3-已完成
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;
}
