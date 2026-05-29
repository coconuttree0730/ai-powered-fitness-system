package com.fitness.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coach_notification")
public class CoachNotification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("coach_id")
    private Long coachId;

    @TableField("student_id")
    private Long studentId;

    @TableField("booking_id")
    private Long bookingId;

    @TableField("type")
    private String type;

    @TableField("content")
    private String content;

    @TableField("is_read")
    private Boolean isRead;
}