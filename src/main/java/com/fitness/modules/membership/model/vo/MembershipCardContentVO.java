package com.fitness.modules.membership.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MembershipCardContentVO {

    private Long id;
    private Long cardId;
    private String contentType;
    private String contentTypeLabel;
    private String title;
    private String description;
    private String icon;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
