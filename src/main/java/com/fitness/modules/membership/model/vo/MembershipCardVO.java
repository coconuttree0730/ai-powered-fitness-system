package com.fitness.modules.membership.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MembershipCardVO {

    private Long id;
    private Long typeId;
    private String typeCode;
    private String typeName;
    private String name;
    private String subtitle;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer durationDays;
    private Integer pointsReward;
    private BigDecimal dailyPrice;
    private String status;
    private String statusLabel;
    private Boolean isRecommend;
    private Integer sortOrder;
    private String coverImage;
    private List<MembershipCardContentVO> contents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
