package com.fitness.modules.booking.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fitness_private_coach_booking")
public class PrivateCoachBooking extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("coach_id")
    private Long coachId;

    @TableField("booking_date")
    private LocalDate bookingDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("note")
    private String note;

    @TableField("status")
    private Integer status;

    @TableField("cancel_reason")
    private String cancelReason;
}