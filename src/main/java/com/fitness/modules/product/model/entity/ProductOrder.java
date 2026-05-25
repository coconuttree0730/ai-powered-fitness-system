package com.fitness.modules.product.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_order")
public class ProductOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal originalPrice;
    private Integer pointsUsed;
    private BigDecimal pointsDiscount;
    private BigDecimal finalPrice;
    private BigDecimal payAmount;
    private String payMethod;
    private LocalDateTime payTime;
    private String status;
    private String trackingNo;
    private String carrier;
    private String address;
    private String remark;
    private Long coachId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    //mybatis plus 配置填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
