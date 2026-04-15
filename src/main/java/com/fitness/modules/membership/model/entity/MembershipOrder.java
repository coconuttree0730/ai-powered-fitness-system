package com.fitness.modules.membership.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("membership_order")
public class MembershipOrder {

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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
