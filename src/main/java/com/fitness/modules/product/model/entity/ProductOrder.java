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

    /** 关联统一订单表 orders.id */
    private Long orderId;

    private String orderNo;
    private Long userId;
    private Long productId;
    private String productImage;
    private BigDecimal originalPrice;
    private BigDecimal payAmount;
    private String payMethod;
    private LocalDateTime payTime;
    private String status;
    private String remark;
    private LocalDateTime expireTime;
    private String alipayTradeNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}