package com.fitness.modules.membership.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserMembershipVO {

    private Long id;
    private Long userId;
    private String membershipType;
    private LocalDateTime startTime;
    private LocalDateTime expireTime;
    private Boolean isActive;
    private Integer remainingDays;
    private Integer totalOrders;
    private LocalDateTime createdAt;
}
