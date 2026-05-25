package com.fitness.modules.booking.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 预约详情VO
 */
@Data
public class BookingVO {

    /**
     * 预约ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 教练名称
     */
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
     * 课程地点
     */
    private String location;

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

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
