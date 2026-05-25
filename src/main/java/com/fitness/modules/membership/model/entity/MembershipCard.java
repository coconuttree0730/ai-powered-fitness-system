package com.fitness.modules.membership.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("membership_card")
public class MembershipCard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long typeId;
    private String typeCode;
    private String name;
    private String subtitle;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer durationDays;
    private Integer pointsReward;
    private BigDecimal dailyPrice;
    private String status;
    private Boolean isRecommend;
    private Integer sortOrder;
    private String coverImage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
