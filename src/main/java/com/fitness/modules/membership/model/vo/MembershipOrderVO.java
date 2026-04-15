package com.fitness.modules.membership.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MembershipOrderVO {

    private Long id;
    private String orderNo;
    private Long cardId;
    private String cardName;
    private BigDecimal price;
    private BigDecimal payAmount;
    private String payMethod;
    private String payMethodLabel;
    private LocalDateTime payTime;
    private String status;
    private String statusLabel;
    private LocalDateTime expireTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
