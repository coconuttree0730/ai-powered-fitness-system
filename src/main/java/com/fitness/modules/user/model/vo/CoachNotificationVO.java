package com.fitness.modules.user.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class CoachNotificationVO {

    private Long id;

    private Long coachId;

    private Long studentId;

    private Long bookingId;

    private String type;

    private String content;

    private Boolean isRead;

    private LocalDateTime createTime;

    private String studentName;

    private LocalDate bookingDate;

    private LocalTime startTime;

    private LocalTime endTime;
}