package com.fitness.modules.order.model.vo;

import com.fitness.modules.order.model.entity.CoachPackageOrderExt;
import com.fitness.modules.order.model.entity.MembershipOrderExt;
import com.fitness.modules.order.model.entity.ProductOrderExt;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private String orderType;
    private String orderTypeLabel;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String payMethod;
    private String payMethodLabel;
    private LocalDateTime payTime;
    private String alipayTradeNo;
    private String status;
    private String statusLabel;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String userName;
    private String userPhone;
    private String userAvatar;

    private ProductOrderExt productExt;
    private CoachPackageOrderExt coachPackageExt;
    private MembershipOrderExt membershipExt;
}