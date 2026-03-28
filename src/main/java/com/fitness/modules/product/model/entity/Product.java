package com.fitness.modules.product.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String code;
    private String category;
    private String imageUrl;
    private String description;
    private BigDecimal originalPrice;
    private String pointsDiscountType;
    private BigDecimal pointsDiscountValue;
    private BigDecimal maxPointsDiscount;
    private Integer stock;
    private Integer sales;
    private String status;
    private Boolean isHot;
    private Boolean isNew;
    private Boolean isRecommend;
    private Integer sortOrder;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
