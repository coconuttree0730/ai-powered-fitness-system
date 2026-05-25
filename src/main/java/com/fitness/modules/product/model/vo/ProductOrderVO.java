package com.fitness.modules.product.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductOrderVO {
    private Long id;
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
    private LocalDateTime payTime;
    private String status;
    private String statusLabel;
    private String trackingNo;
    private String carrier;
    private String address;
    private String remark;
    private Long coachId;
    private LocalDateTime createdAt;
}
