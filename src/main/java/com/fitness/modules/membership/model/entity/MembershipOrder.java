package com.fitness.modules.membership.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("membership_order")
public class MembershipOrder extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long cardId;
    private String cardName;
    private BigDecimal price;
    private BigDecimal payAmount;
    private String payMethod;
    private LocalDateTime payTime;
    private String status;
    private LocalDateTime expireTime;
    private String alipayTradeNo;
    private String remark;
}
