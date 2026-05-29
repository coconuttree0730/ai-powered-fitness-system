package com.fitness.modules.product.model.vo;

import com.fitness.common.sensitive.Sensitive;
import com.fitness.common.sensitive.SensitiveType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductOrderVO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal originalPrice;
    private Integer pointsUsed;
    private BigDecimal pointsDiscount;
    private BigDecimal finalPrice;
    private BigDecimal payAmount;
    private String payMethod;
    private String payMethodLabel;
    private LocalDateTime payTime;
    private String status;
    private String statusLabel;
    private String trackingNo;
    private String carrier;
    @Sensitive(SensitiveType.ADDRESS)
    private String address;
    private String remark;
    private Long coachId;
    private String pickupType;
    private String pickupCode;
    private String pickupStatus;
    private String pickupStatusLabel;
    private LocalDateTime pickupTime;
    private LocalDateTime expireTime;
    private String alipayTradeNo;
    private Long remainingSeconds;
    private LocalDateTime createdAt;
}
