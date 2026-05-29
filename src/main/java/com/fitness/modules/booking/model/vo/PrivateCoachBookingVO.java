package com.fitness.modules.booking.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class PrivateCoachBookingVO {

    private Long id;

    private Long userId;

    private String userName;

    private Long coachId;

    private String coachName;

    private LocalDate bookingDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String note;

    private Integer status;

    private String statusDesc;

    private LocalDateTime createTime;
}