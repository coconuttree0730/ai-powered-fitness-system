package com.fitness.modules.booking.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约列表VO
 */
@Data
public class BookingListVO {

    private Long id;

    private Long courseId;
    private String courseName;
    private String coachName;

    /**
     * 课程实例ID
     */
    private Long sessionId;

    /**
     * 上课日期（具体某一天）
     */
    private LocalDate sessionDate;

    /**
     * 星期几
     */
    private Integer dayOfWeek;

    /**
     * 开始时间（时分秒）
     */
    private LocalTime startTime;

    /**
     * 结束时间（时分秒）
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
