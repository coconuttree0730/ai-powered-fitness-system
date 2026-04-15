package com.fitness.modules.membership.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MembershipCardTypeVO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private String status;
    private String statusLabel;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
