package com.fitness.modules.order.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_order_ext")
public class ProductOrderExt {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer pointsUsed;
    private BigDecimal pointsDiscount;
    private String trackingNo;
    private String carrier;
    private String address;
    private String pickupType;
    private String pickupCode;
    private String pickupStatus;
    private LocalDateTime pickupTime;
}