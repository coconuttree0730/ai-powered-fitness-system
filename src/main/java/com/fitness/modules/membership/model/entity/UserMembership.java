package com.fitness.modules.membership.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_membership")
public class UserMembership {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String membershipType;
    private LocalDateTime startTime;
    private LocalDateTime expireTime;
    private Boolean isActive;
    private Integer totalOrders;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
