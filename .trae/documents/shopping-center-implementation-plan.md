# 购物中心重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将积分商城重构为购物中心，实现"商品原价+积分抵扣"的混合支付方案

**Architecture:** 
- 前端：修改会员端Store.vue页面，重构商品展示和支付流程UI
- 后端：新增商品管理、订单管理相关Entity、Mapper、Service、Controller
- 数据库：新增商品表、商品订单表，支持混合支付模式

**Tech Stack:** Vue3 + Naive UI + Spring Boot + MyBatis-Plus + PostgreSQL

---

## 任务清单概览

### 第一阶段：文本替换（积分商城 → 购物中心）
### 第二阶段：数据库设计
### 第三阶段：后端API开发
### 第四节段：前端页面重构

---

## 第一阶段：文本替换

### Task 1: 替换导航栏和路由中的"积分商城"文本

**Files:**
- Modify: `frontend/src/layouts/MemberLayout.vue:218`
- Modify: `frontend/src/router/index.js:96`

- [ ] **Step 1: 修改MemberLayout.vue菜单文本**

```javascript
// 将
{ label: '积分商城', key: '/member/store', iconComponent: GiftOutline }
// 改为
{ label: '购物中心', key: '/member/store', iconComponent: GiftOutline }
```

- [ ] **Step 2: 修改路由meta标题**

```javascript
// 将
meta: { title: '积分商城' }
// 改为
meta: { title: '购物中心' }
```

---

### Task 2: 替换管理端内容管理中的"积分商城"文本

**Files:**
- Modify: `frontend/src/views/admin/ContentManagement.vue:233`
- Modify: `frontend/src/views/admin/ContentManagement.vue:258`
- Modify: `frontend/src/views/admin/ContentManagement.vue:764-818`

- [ ] **Step 1: 修改标签页标题**

```vue
<!-- 将 -->
<el-tab-pane name="store-display">
  <template #label>
    <span class="tab-label">
      <el-icon><ShoppingCart /></el-icon>积分商城展示
    </span>
  </template>

<!-- 改为 -->
<el-tab-pane name="store-display">
  <template #label>
    <span class="tab-label">
      <el-icon><ShoppingCart /></el-icon>购物中心展示
    </span>
  </template>
```

- [ ] **Step 2: 修改搜索框placeholder**

```vue
<!-- 将 -->
<el-input v-model="storeDisplaySearch.name" placeholder="搜索商品名称" />

<!-- 改为 -->
<el-input v-model="storeDisplaySearch.name" placeholder="搜索商品" />
```

- [ ] **Step 3: 修改统计标题**

```javascript
// 将
{ title: '商城商品展示', value: 8, icon: 'ShoppingCart', color: '#722ed1' }
// 改为
{ title: '购物中心商品', value: 8, icon: 'ShoppingCart', color: '#722ed1' }
```

- [ ] **Step 4: 修改注释文本**

```javascript
// 将
// ========== 积分商城展示管理 ==========
// 改为
// ========== 购物中心展示管理 ==========
```

---

### Task 3: 替换订单管理中的"积分兑换"相关文本

**Files:**
- Modify: `frontend/src/views/admin/OrderManagement.vue:134-233`

- [ ] **Step 1: 修改标签页标题**

```vue
<!-- 将 -->
<el-tab-pane name="points-exchange">
  <template #label>
    <span class="tab-label">
      <el-icon><Coin /></el-icon>积分兑换
    </span>
  </template>

<!-- 改为 -->
<el-tab-pane name="product-order">
  <template #label>
    <span class="tab-label">
      <el-icon><ShoppingCart /></el-icon>商品订单
    </span>
  </template>
</el-tab-pane>
```

- [ ] **Step 2: 修改统计标题**

```javascript
// 将
{ title: '积分兑换', value: 12, icon: 'Coin', color: '#722ed1' }
// 改为
{ title: '商品订单', value: 12, icon: 'ShoppingCart', color: '#722ed1' }
```

---

## 第二阶段：数据库设计

### Task 4: 创建商品表迁移脚本

**Files:**
- Create: `src/main/resources/db/migration/V12__create_product_table.sql`

- [ ] **Step 1: 创建商品表**

```sql
-- 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    code VARCHAR(50) UNIQUE COMMENT '商品编号',
    category VARCHAR(50) NOT NULL COMMENT '商品分类: EQUIPMENT-运动装备, SUPPLEMENT-营养补剂, COURSE-课程优惠, OTHER-其他',
    image_url VARCHAR(500) COMMENT '商品图片URL',
    description TEXT COMMENT '商品描述',
    original_price DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '商品原价',
    points_discount_type VARCHAR(20) DEFAULT 'FIXED' COMMENT '积分抵扣类型: FIXED-固定金额, PERCENT-比例',
    points_discount_value DECIMAL(10, 2) DEFAULT 0 COMMENT '积分抵扣值(固定金额或比例)',
    max_points_discount DECIMAL(10, 2) DEFAULT 0 COMMENT '最大积分抵扣金额',
    stock INTEGER NOT NULL DEFAULT 0 COMMENT '库存数量',
    sales INTEGER DEFAULT 0 COMMENT '销量',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-上架, INACTIVE-下架',
    is_hot BOOLEAN DEFAULT FALSE COMMENT '是否热销',
    is_new BOOLEAN DEFAULT FALSE COMMENT '是否新品',
    is_recommend BOOLEAN DEFAULT FALSE COMMENT '是否推荐',
    sort_order INTEGER DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE product IS '商品表';
```

- [ ] **Step 2: 创建商品订单表**

```sql
-- 商品订单表
CREATE TABLE IF NOT EXISTS product_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) UNIQUE NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    quantity INTEGER NOT NULL DEFAULT 1 COMMENT '购买数量',
    original_price DECIMAL(10, 2) NOT NULL COMMENT '商品原价',
    points_used INTEGER DEFAULT 0 COMMENT '使用积分数量',
    points_discount DECIMAL(10, 2) DEFAULT 0 COMMENT '积分抵扣金额',
    final_price DECIMAL(10, 2) NOT NULL COMMENT '最终支付价格',
    pay_amount DECIMAL(10, 2) NOT NULL COMMENT '实际支付金额',
    pay_method VARCHAR(50) COMMENT '支付方式',
    pay_time TIMESTAMP WITHOUT TIME ZONE COMMENT '支付时间',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态: PENDING-待支付, PAID-已支付, PROCESSING-处理中, SHIPPED-已发货, COMPLETED-已完成, CANCELLED-已取消',
    tracking_no VARCHAR(100) COMMENT '物流单号',
    carrier VARCHAR(50) COMMENT '物流公司',
    address TEXT COMMENT '收货地址',
    remark TEXT COMMENT '备注',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE product_order IS '商品订单表';

CREATE INDEX idx_product_order_user_id ON product_order(user_id);
CREATE INDEX idx_product_order_status ON product_order(status);
CREATE INDEX idx_product_order_created_at ON product_order(created_at);
```

---

## 第三阶段：后端API开发

### Task 5: 创建商品相关实体类

**Files:**
- Create: `src/main/java/com/fitness/modules/product/model/entity/Product.java`
- Create: `src/main/java/com/fitness/modules/product/model/entity/ProductOrder.java`

- [ ] **Step 1: 创建Product实体类**

```java
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
```

- [ ] **Step 2: 创建ProductOrder实体类**

```java
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
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

---

### Task 6: 创建DTO和VO类

**Files:**
- Create: `src/main/java/com/fitness/modules/product/model/dto/ProductDTO.java`
- Create: `src/main/java/com/fitness/modules/product/model/dto/ProductOrderDTO.java`
- Create: `src/main/java/com/fitness/modules/product/model/dto/CalculatePriceDTO.java`
- Create: `src/main/java/com/fitness/modules/product/model/vo/ProductVO.java`
- Create: `src/main/java/com/fitness/modules/product/model/vo/ProductOrderVO.java`
- Create: `src/main/java/com/fitness/modules/product/model/vo/PriceCalculationVO.java`

- [ ] **Step 1: 创建ProductDTO**

```java
package com.fitness.modules.product.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String name;
    
    @Size(max = 50, message = "商品编号不能超过50个字符")
    private String code;
    
    @NotBlank(message = "商品分类不能为空")
    private String category;
    
    private String imageUrl;
    private String description;
    
    @NotNull(message = "商品原价不能为空")
    @DecimalMin(value = "0.01", message = "商品原价必须大于0")
    private BigDecimal originalPrice;
    
    private String pointsDiscountType;
    private BigDecimal pointsDiscountValue;
    private BigDecimal maxPointsDiscount;
    
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
    
    private String status;
    private Boolean isHot;
    private Boolean isNew;
    private Boolean isRecommend;
    private Integer sortOrder;
}
```

- [ ] **Step 2: 创建ProductOrderDTO**

```java
package com.fitness.modules.product.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductOrderDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity;
    
    private Integer usePoints;
    private String address;
    private String remark;
}
```

- [ ] **Step 3: 创建CalculatePriceDTO**

```java
package com.fitness.modules.product.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CalculatePriceDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity;
    
    private Integer usePoints;
}
```

- [ ] **Step 4: 创建ProductVO**

```java
package com.fitness.modules.product.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private String code;
    private String category;
    private String categoryLabel;
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
    private LocalDateTime createdAt;
    
    // 前端展示用字段
    private BigDecimal calculatedDiscount;
    private BigDecimal finalPrice;
    private Integer requiredPoints;
}
```

- [ ] **Step 5: 创建ProductOrderVO**

```java
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
    private LocalDateTime createdAt;
}
```

- [ ] **Step 6: 创建PriceCalculationVO**

```java
package com.fitness.modules.product.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceCalculationVO {
    private Long productId;
    private Integer quantity;
    private BigDecimal originalTotalPrice;
    private Integer userAvailablePoints;
    private Integer usePoints;
    private BigDecimal pointsDiscount;
    private BigDecimal finalPrice;
    private Boolean pointsSufficient;
    private String message;
}
```

---

### Task 7: 创建Mapper接口

**Files:**
- Create: `src/main/java/com/fitness/modules/product/mapper/ProductMapper.java`
- Create: `src/main/java/com/fitness/modules/product/mapper/ProductOrderMapper.java`

- [ ] **Step 1: 创建ProductMapper**

```java
package com.fitness.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.product.model.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    @Update("UPDATE product SET stock = stock - #{quantity}, sales = sales + #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    @Update("UPDATE product SET stock = stock + #{quantity}, sales = sales - #{quantity} WHERE id = #{productId}")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
```

- [ ] **Step 2: 创建ProductOrderMapper**

```java
package com.fitness.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.product.model.entity.ProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductOrderMapper extends BaseMapper<ProductOrder> {
    
    @Select("SELECT * FROM product_order WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ProductOrder> selectByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM product_order WHERE order_no = #{orderNo}")
    ProductOrder selectByOrderNo(@Param("orderNo") String orderNo);
}
```

---

### Task 8: 创建Service接口和实现

**Files:**
- Create: `src/main/java/com/fitness/modules/product/service/ProductService.java`
- Create: `src/main/java/com/fitness/modules/product/service/impl/ProductServiceImpl.java`
- Create: `src/main/java/com/fitness/modules/product/service/ProductOrderService.java`
- Create: `src/main/java/com/fitness/modules/product/service/impl/ProductOrderServiceImpl.java`

- [ ] **Step 1: 创建ProductService接口**

```java
package com.fitness.modules.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.product.model.dto.ProductDTO;
import com.fitness.modules.product.model.entity.Product;
import com.fitness.modules.product.model.vo.ProductVO;

import java.util.List;

public interface ProductService extends IService<Product> {
    
    List<ProductVO> getProductList(String category);
    
    ProductVO getProductDetail(Long id);
    
    ProductVO createProduct(ProductDTO dto);
    
    ProductVO updateProduct(ProductDTO dto);
    
    void deleteProduct(Long id);
    
    void updateProductStatus(Long id, String status);
}
```

- [ ] **Step 2: 创建ProductServiceImpl**

```java
package com.fitness.modules.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.ErrorCode;
import com.fitness.modules.product.mapper.ProductMapper;
import com.fitness.modules.product.model.dto.ProductDTO;
import com.fitness.modules.product.model.entity.Product;
import com.fitness.modules.product.model.vo.ProductVO;
import com.fitness.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    
    private final ProductMapper productMapper;
    
    @Override
    public List<ProductVO> getProductList(String category) {
        List<Product> products;
        if (category == null || "all".equals(category)) {
            products = lambdaQuery()
                .eq(Product::getStatus, "ACTIVE")
                .orderByDesc(Product::getSortOrder)
                .list();
        } else {
            products = lambdaQuery()
                .eq(Product::getStatus, "ACTIVE")
                .eq(Product::getCategory, category)
                .orderByDesc(Product::getSortOrder)
                .list();
        }
        return products.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public ProductVO getProductDetail(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return convertToVO(product);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO createProduct(ProductDTO dto) {
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        save(product);
        return convertToVO(product);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO updateProduct(ProductDTO dto) {
        Product product = getById(dto.getId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        BeanUtil.copyProperties(dto, product);
        updateById(product);
        return convertToVO(product);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        removeById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStatus(Long id, String status) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        product.setStatus(status);
        updateById(product);
    }
    
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        vo.setCategoryLabel(getCategoryLabel(product.getCategory()));
        return vo;
    }
    
    private String getCategoryLabel(String category) {
        return switch (category) {
            case "EQUIPMENT" -> "运动装备";
            case "SUPPLIMENT" -> "营养补剂";
            case "COURSE" -> "课程优惠";
            case "OTHER" -> "其他";
            default -> category;
        };
    }
}
```

- [ ] **Step 3: 创建ProductOrderService接口**

```java
package com.fitness.modules.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.product.model.dto.CalculatePriceDTO;
import com.fitness.modules.product.model.dto.ProductOrderDTO;
import com.fitness.modules.product.model.entity.ProductOrder;
import com.fitness.modules.product.model.vo.PriceCalculationVO;
import com.fitness.modules.product.model.vo.ProductOrderVO;

import java.util.List;

public interface ProductOrderService extends IService<ProductOrder> {
    
    PriceCalculationVO calculatePrice(CalculatePriceDTO dto, Long userId);
    
    ProductOrderVO createOrder(ProductOrderDTO dto, Long userId);
    
    ProductOrderVO payOrder(String orderNo, String payMethod);
    
    List<ProductOrderVO> getUserOrders(Long userId);
    
    ProductOrderVO getOrderDetail(String orderNo);
    
    void cancelOrder(String orderNo);
    
    void shipOrder(String orderNo, String trackingNo, String carrier);
    
    void completeOrder(String orderNo);
}
```

- [ ] **Step 4: 创建ProductOrderServiceImpl**

```java
package com.fitness.modules.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.ErrorCode;
import com.fitness.modules.product.mapper.ProductMapper;
import com.fitness.modules.product.mapper.ProductOrderMapper;
import com.fitness.modules.product.model.dto.CalculatePriceDTO;
import com.fitness.modules.product.model.dto.ProductOrderDTO;
import com.fitness.modules.product.model.entity.Product;
import com.fitness.modules.product.model.entity.ProductOrder;
import com.fitness.modules.product.model.vo.PriceCalculationVO;
import com.fitness.modules.product.model.vo.ProductOrderVO;
import com.fitness.modules.product.service.ProductOrderService;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder> implements ProductOrderService {
    
    private final ProductOrderMapper productOrderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    
    private static final BigDecimal POINTS_TO_MONEY_RATE = new BigDecimal("0.01");
    
    @Override
    public PriceCalculationVO calculatePrice(CalculatePriceDTO dto, Long userId) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        
        User user = userMapper.selectById(userId);
        Integer userPoints = user.getPoints() != null ? user.getPoints() : 0;
        
        BigDecimal originalPrice = product.getOriginalPrice();
        BigDecimal totalOriginalPrice = originalPrice.multiply(new BigDecimal(dto.getQuantity()));
        
        PriceCalculationVO vo = new PriceCalculationVO();
        vo.setProductId(dto.getProductId());
        vo.setQuantity(dto.getQuantity());
        vo.setOriginalTotalPrice(totalOriginalPrice);
        vo.setUserAvailablePoints(userPoints);
        
        // 计算积分抵扣
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        Integer usePoints = dto.getUsePoints() != null ? dto.getUsePoints() : 0;
        
        if (usePoints > 0 && "FIXED".equals(product.getPointsDiscountType())) {
            // 固定金额抵扣
            BigDecimal maxDiscount = product.getMaxPointsDiscount() != null ? 
                product.getMaxPointsDiscount() : totalOriginalPrice;
            BigDecimal requestedDiscount = new BigDecimal(usePoints).multiply(POINTS_TO_MONEY_RATE);
            pointsDiscount = requestedDiscount.min(maxDiscount).min(totalOriginalPrice);
        } else if (usePoints > 0 && "PERCENT".equals(product.getPointsDiscountType())) {
            // 比例抵扣
            BigDecimal percent = product.getPointsDiscountValue() != null ? 
                product.getPointsDiscountValue() : BigDecimal.ZERO;
            BigDecimal maxDiscount = totalOriginalPrice.multiply(percent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal requestedDiscount = new BigDecimal(usePoints).multiply(POINTS_TO_MONEY_RATE);
            pointsDiscount = requestedDiscount.min(maxDiscount).min(totalOriginalPrice);
        }
        
        Integer requiredPoints = pointsDiscount.divide(POINTS_TO_MONEY_RATE, 0, RoundingMode.CEILING).intValue();
        
        vo.setUsePoints(requiredPoints);
        vo.setPointsDiscount(pointsDiscount);
        vo.setFinalPrice(totalOriginalPrice.subtract(pointsDiscount));
        vo.setPointsSufficient(userPoints >= requiredPoints);
        
        if (!vo.getPointsSufficient()) {
            vo.setMessage("积分不足，当前可用积分: " + userPoints);
        } else {
            vo.setMessage("积分抵扣成功，可抵扣 " + pointsDiscount + " 元");
        }
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductOrderVO createOrder(ProductOrderDTO dto, Long userId) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        
        if (product.getStock() < dto.getQuantity()) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }
        
        // 计算价格
        CalculatePriceDTO calculateDTO = new CalculatePriceDTO();
        calculateDTO.setProductId(dto.getProductId());
        calculateDTO.setQuantity(dto.getQuantity());
        calculateDTO.setUsePoints(dto.getUsePoints());
        PriceCalculationVO priceCalc = calculatePrice(calculateDTO, userId);
        
        if (!priceCalc.getPointsSufficient()) {
            throw new BusinessException(ErrorCode.POINTS_INSUFFICIENT);
        }
        
        // 扣减库存
        int affected = productMapper.decreaseStock(dto.getProductId(), dto.getQuantity());
        if (affected == 0) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }
        
        // 扣减积分
        User user = userMapper.selectById(userId);
        user.setPoints(user.getPoints() - priceCalc.getUsePoints());
        userMapper.updateById(user);
        
        // 创建订单
        ProductOrder order = new ProductOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(dto.getProductId());
        order.setProductName(product.getName());
        order.setQuantity(dto.getQuantity());
        order.setOriginalPrice(product.getOriginalPrice());
        order.setPointsUsed(priceCalc.getUsePoints());
        order.setPointsDiscount(priceCalc.getPointsDiscount());
        order.setFinalPrice(priceCalc.getFinalPrice());
        order.setPayAmount(priceCalc.getFinalPrice());
        order.setStatus("PENDING");
        order.setAddress(dto.getAddress());
        order.setRemark(dto.getRemark());
        
        save(order);
        
        return convertToVO(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductOrderVO payOrder(String orderNo, String payMethod) {
        ProductOrder order = productOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        }
        
        order.setStatus("PAID");
        order.setPayMethod(payMethod);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
        
        return convertToVO(order);
    }
    
    @Override
    public List<ProductOrderVO> getUserOrders(Long userId) {
        List<ProductOrder> orders = lambdaQuery()
            .eq(ProductOrder::getUserId, userId)
            .orderByDesc(ProductOrder::getCreatedAt)
            .list();
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public ProductOrderVO getOrderDetail(String orderNo) {
        ProductOrder order = productOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return convertToVO(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        ProductOrder order = productOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        }
        
        // 恢复库存
        productMapper.increaseStock(order.getProductId(), order.getQuantity());
        
        // 恢复积分
        User user = userMapper.selectById(order.getUserId());
        user.setPoints(user.getPoints() + order.getPointsUsed());
        userMapper.updateById(user);
        
        order.setStatus("CANCELLED");
        updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo, String trackingNo, String carrier) {
        ProductOrder order = productOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        order.setStatus("SHIPPED");
        order.setTrackingNo(trackingNo);
        order.setCarrier(carrier);
        updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(String orderNo) {
        ProductOrder order = productOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        order.setStatus("COMPLETED");
        updateById(order);
    }
    
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%04d", new Random().nextInt(10000));
        return "PO" + dateStr + randomStr;
    }
    
    private ProductOrderVO convertToVO(ProductOrder order) {
        ProductOrderVO vo = new ProductOrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setStatusLabel(getStatusLabel(order.getStatus()));
        return vo;
    }
    
    private String getStatusLabel(String status) {
        return switch (status) {
            case "PENDING" -> "待支付";
            case "PAID" -> "已支付";
            case "PROCESSING" -> "处理中";
            case "SHIPPED" -> "已发货";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }
}
```

---

### Task 9: 创建Controller

**Files:**
- Create: `src/main/java/com/fitness/modules/product/controller/ProductController.java`
- Create: `src/main/java/com/fitness/modules/product/controller/ProductOrderController.java`
- Create: `src/main/java/com/fitness/modules/product/controller/ProductAdminController.java`

- [ ] **Step 1: 创建ProductController（会员端）**

```java
package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.product.model.vo.ProductVO;
import com.fitness.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public Result<List<ProductVO>> list(@RequestParam(required = false) String category) {
        return Result.success(productService.getProductList(category));
    }
    
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }
}
```

- [ ] **Step 2: 创建ProductOrderController（会员端）**

```java
package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.product.model.dto.CalculatePriceDTO;
import com.fitness.modules.product.model.dto.ProductOrderDTO;
import com.fitness.modules.product.model.vo.PriceCalculationVO;
import com.fitness.modules.product.model.vo.ProductOrderVO;
import com.fitness.modules.product.service.ProductOrderService;
import com.fitness.modules.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-orders")
@RequiredArgsConstructor
public class ProductOrderController {
    
    private final ProductOrderService productOrderService;
    
    @PostMapping("/calculate")
    public Result<PriceCalculationVO> calculatePrice(@Valid @RequestBody CalculatePriceDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(productOrderService.calculatePrice(dto, userId));
    }
    
    @PostMapping
    public Result<ProductOrderVO> createOrder(@Valid @RequestBody ProductOrderDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(productOrderService.createOrder(dto, userId));
    }
    
    @GetMapping
    public Result<List<ProductOrderVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(productOrderService.getUserOrders(userId));
    }
    
    @GetMapping("/{orderNo}")
    public Result<ProductOrderVO> detail(@PathVariable String orderNo) {
        return Result.success(productOrderService.getOrderDetail(orderNo));
    }
    
    @PostMapping("/{orderNo}/pay")
    public Result<ProductOrderVO> pay(@PathVariable String orderNo, @RequestParam String payMethod) {
        return Result.success(productOrderService.payOrder(orderNo, payMethod));
    }
    
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        productOrderService.cancelOrder(orderNo);
        return Result.success();
    }
}
```

- [ ] **Step 3: 创建ProductAdminController（管理端）**

```java
package com.fitness.modules.product.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.modules.product.model.dto.ProductDTO;
import com.fitness.modules.product.model.vo.ProductVO;
import com.fitness.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {
    
    private final ProductService productService;
    
    @GetMapping
    public Result<List<ProductVO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        // 简化实现，实际应使用分页
        return Result.success(productService.getProductList(category));
    }
    
    @PostMapping
    public Result<ProductVO> create(@Valid @RequestBody ProductDTO dto) {
        return Result.success(productService.createProduct(dto));
    }
    
    @PutMapping("/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        dto.setId(id);
        return Result.success(productService.updateProduct(dto));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        productService.updateProductStatus(id, status);
        return Result.success();
    }
}
```

---

### Task 10: 添加错误码

**Files:**
- Modify: `src/main/java/com/fitness/common/constants/ErrorCode.java`

- [ ] **Step 1: 添加商品和订单相关错误码**

```java
// 在ErrorCode枚举中添加
PRODUCT_NOT_FOUND(3001, "商品不存在"),
PRODUCT_STOCK_INSUFFICIENT(3002, "商品库存不足"),
POINTS_INSUFFICIENT(3003, "积分不足"),
ORDER_NOT_FOUND(4001, "订单不存在"),
ORDER_STATUS_ERROR(4002, "订单状态错误");
```

---

## 第四阶段：前端页面重构

### Task 11: 重构会员端Store.vue页面

**Files:**
- Modify: `frontend/src/views/member/Store.vue`

- [ ] **Step 1: 修改模板结构 - 商品卡片展示**

```vue
<!-- 商品卡片 - 显示原价、积分抵扣、最终价格 -->
<n-grid-item v-for="product in filteredProducts" :key="product.id">
  <div class="product-card" @click="openProductDetail(product)">
    <div class="product-image" :style="{ background: product.gradient }">
      <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" />
      <div v-else class="product-icon" v-html="product.icon"></div>
    </div>
    <div class="product-info">
      <div class="product-name">{{ product.name }}</div>
      <div class="product-desc">{{ product.description }}</div>
      
      <!-- 价格展示区域 -->
      <div class="price-section">
        <div class="original-price">¥{{ product.originalPrice }}</div>
        <div class="discount-info" v-if="product.pointsDiscountValue > 0">
          <span class="discount-tag">积分抵扣</span>
          <span class="discount-amount">-¥{{ product.calculatedDiscount }}</span>
        </div>
        <div class="final-price">
          <span class="price-label">到手价</span>
          <span class="price-value">¥{{ product.finalPrice }}</span>
          <span class="points-needed" v-if="product.requiredPoints > 0">
            或 {{ product.requiredPoints }} 积分
          </span>
        </div>
      </div>
      
      <n-button 
        type="primary" 
        size="small" 
        class="buy-btn"
        :disabled="product.stock <= 0"
        @click.stop="openOrderModal(product)"
      >
        {{ product.stock > 0 ? '立即购买' : '暂时缺货' }}
      </n-button>
    </div>
  </div>
</n-grid-item>
```

- [ ] **Step 2: 修改购买弹窗 - 支持混合支付**

```vue
<!-- 购买确认弹窗 -->
<n-modal v-model:show="showOrderModal" preset="card" style="width: 500px" :show-header="false">
  <div class="order-content">
    <div class="order-header">
      <svg class="cart-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="9" cy="21" r="1"/>
        <circle cx="20" cy="21" r="1"/>
        <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
      </svg>
      <span>确认购买</span>
    </div>
    
    <div v-if="selectedProduct" class="order-product">
      <div class="product-image-small" :style="{ background: selectedProduct.gradient }">
        <img v-if="selectedProduct.imageUrl" :src="selectedProduct.imageUrl" />
        <div v-else v-html="selectedProduct.icon"></div>
      </div>
      <div class="product-info-small">
        <h4>{{ selectedProduct.name }}</h4>
        <p>{{ selectedProduct.description }}</p>
      </div>
    </div>
    
    <n-divider />
    
    <!-- 数量选择 -->
    <div class="quantity-section">
      <span>购买数量</span>
      <n-input-number v-model:value="orderQuantity" :min="1" :max="selectedProduct?.stock || 1" />
    </div>
    
    <n-divider />
    
    <!-- 价格计算明细 -->
    <div class="price-detail">
      <div class="detail-row">
        <span>商品原价</span>
        <span class="original">¥{{ calculatedPrice.originalTotalPrice }}</span>
      </div>
      
      <!-- 积分抵扣滑块 -->
      <div class="points-section" v-if="userPoints > 0 && selectedProduct?.pointsDiscountValue > 0">
        <div class="points-header">
          <span>使用积分抵扣</span>
          <span class="points-available">可用积分: {{ userPoints }}</span>
        </div>
        <n-slider 
          v-model:value="usePoints" 
          :max="maxUsablePoints" 
          :step="100"
          :disabled="!canUsePoints"
        />
        <div class="points-input">
          <span>使用 {{ usePoints }} 积分</span>
          <span class="discount-amount">-¥{{ calculatedPrice.pointsDiscount }}</span>
        </div>
        <n-alert v-if="!calculatedPrice.pointsSufficient" type="warning" :show-icon="false">
          积分不足，当前可用积分: {{ userPoints }}
        </n-alert>
      </div>
      
      <div class="detail-row total">
        <span>最终支付</span>
        <span class="final">¥{{ calculatedPrice.finalPrice }}</span>
      </div>
    </div>
    
    <!-- 支付方式 -->
    <div class="pay-method-section">
      <span>支付方式</span>
      <n-radio-group v-model:value="payMethod">
        <n-radio-button value="WECHAT">微信支付</n-radio-button>
        <n-radio-button value="ALIPAY">支付宝</n-radio-button>
      </n-radio-group>
    </div>
  </div>
  
  <template #footer>
    <n-space justify="end">
      <n-button @click="showOrderModal = false">取消</n-button>
      <n-button 
        type="primary" 
        :loading="submitting"
        :disabled="!calculatedPrice.pointsSufficient || selectedProduct?.stock <= 0"
        @click="confirmOrder"
      >
        确认支付 ¥{{ calculatedPrice.finalPrice }}
      </n-button>
    </n-space>
  </template>
</n-modal>
```

- [ ] **Step 3: 修改订单记录表格**

```vue
<!-- 订单记录 -->
<div class="card-section" style="margin-top: 32px;">
  <div class="section-header">
    <h3 class="section-title">我的订单</h3>
  </div>
  <n-data-table
    :columns="orderColumns"
    :data="orderRecords"
    :pagination="{ pageSize: 5 }"
    :bordered="false"
    class="record-table"
  />
</div>
```

- [ ] **Step 4: 修改Script逻辑**

```javascript
import { ref, computed, h, watch } from 'vue'
import { useMessage, NTag, NButton } from 'naive-ui'
import { getProducts, calculatePrice, createOrder, getOrders } from '@/api/product'

const message = useMessage()

const activeTab = ref('all')
const userPoints = ref(580)
const showOrderModal = ref(false)
const submitting = ref(false)
const selectedProduct = ref(null)
const orderQuantity = ref(1)
const usePoints = ref(0)
const payMethod = ref('WECHAT')
const products = ref([])
const orderRecords = ref([])

// 计算价格
const calculatedPrice = ref({
  originalTotalPrice: 0,
  pointsDiscount: 0,
  finalPrice: 0,
  pointsSufficient: true
})

// 最大可用积分
const maxUsablePoints = computed(() => {
  if (!selectedProduct.value) return 0
  return Math.min(userPoints.value, selectedProduct.value.maxPointsDiscount * 100)
})

// 是否可以使用积分
const canUsePoints = computed(() => {
  return userPoints.value > 0 && selectedProduct.value?.pointsDiscountValue > 0
})

// 监听积分使用变化
watch([() => selectedProduct.value, orderQuantity, usePoints], async () => {
  if (selectedProduct.value) {
    await recalculatePrice()
  }
}, { immediate: true })

async function recalculatePrice() {
  try {
    const res = await calculatePrice({
      productId: selectedProduct.value.id,
      quantity: orderQuantity.value,
      usePoints: usePoints.value
    })
    calculatedPrice.value = res
  } catch (error) {
    message.error('价格计算失败')
  }
}

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'EQUIPMENT', label: '运动装备' },
  { key: 'SUPPLEMENT', label: '营养补剂' },
  { key: 'COURSE', label: '课程优惠' },
  { key: 'OTHER', label: '其他' }
]

const filteredProducts = computed(() => {
  if (activeTab.value === 'all') {
    return products.value
  }
  return products.value.filter(p => p.category === activeTab.value)
})

const orderColumns = [
  { title: '订单号', key: 'orderNo', width: 150 },
  { title: '日期', key: 'createdAt', width: 120 },
  { title: '商品名称', key: 'productName' },
  { 
    title: '原价', 
    key: 'originalPrice',
    width: 100,
    render(row) {
      return h('span', { style: { textDecoration: 'line-through', color: '#999' } }, 
        `¥${row.originalPrice * row.quantity}`)
    }
  },
  { 
    title: '积分抵扣', 
    key: 'pointsDiscount',
    width: 100,
    render(row) {
      return h('span', { style: { color: '#FF6B35' } }, 
        row.pointsDiscount > 0 ? `-¥${row.pointsDiscount}` : '-')
    }
  },
  { 
    title: '实付金额', 
    key: 'payAmount',
    width: 100,
    render(row) {
      return h('span', { style: { color: '#FF6B35', fontWeight: 600 } }, `¥${row.payAmount}`)
    }
  },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    render(row) {
      const statusMap = {
        'PENDING': { type: 'warning', text: '待支付' },
        'PAID': { type: 'processing', text: '已支付' },
        'SHIPPED': { type: 'success', text: '已发货' },
        'COMPLETED': { type: 'default', text: '已完成' },
        'CANCELLED': { type: 'error', text: '已取消' }
      }
      const status = statusMap[row.status]
      return h(NTag, { type: status.type, size: 'small' }, { default: () => status.text })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render(row) {
      if (row.status === 'PENDING') {
        return h(NButton, { 
          type: 'primary', 
          size: 'small',
          onClick: () => handlePay(row)
        }, { default: () => '去支付' })
      }
      return null
    }
  }
]

function openOrderModal(product) {
  selectedProduct.value = product
  orderQuantity.value = 1
  usePoints.value = 0
  showOrderModal.value = true
}

async function confirmOrder() {
  submitting.value = true
  try {
    const res = await createOrder({
      productId: selectedProduct.value.id,
      quantity: orderQuantity.value,
      usePoints: usePoints.value,
      address: ''
    })
    
    // 调用支付
    await payOrder(res.orderNo)
    
    message.success('订单创建成功')
    showOrderModal.value = false
    await loadData()
  } catch (error) {
    message.error(error.message || '订单创建失败')
  } finally {
    submitting.value = false
  }
}

async function payOrder(orderNo) {
  // 模拟支付
  message.success('支付成功')
}

async function loadData() {
  try {
    products.value = await getProducts()
    orderRecords.value = await getOrders()
  } catch (error) {
    message.error('数据加载失败')
  }
}

onMounted(() => {
  loadData()
})
```

---

### Task 12: 创建前端API模块

**Files:**
- Create: `frontend/src/api/product.js`

- [ ] **Step 1: 创建商品相关API**

```javascript
import request from '@/utils/request'

// 获取商品列表
export function getProducts(category) {
  return request({
    url: '/api/v1/products',
    method: 'get',
    params: { category }
  })
}

// 获取商品详情
export function getProductDetail(id) {
  return request({
    url: `/api/v1/products/${id}`,
    method: 'get'
  })
}

// 计算价格
export function calculatePrice(data) {
  return request({
    url: '/api/v1/product-orders/calculate',
    method: 'post',
    data
  })
}

// 创建订单
export function createOrder(data) {
  return request({
    url: '/api/v1/product-orders',
    method: 'post',
    data
  })
}

// 获取订单列表
export function getOrders() {
  return request({
    url: '/api/v1/product-orders',
    method: 'get'
  })
}

// 获取订单详情
export function getOrderDetail(orderNo) {
  return request({
    url: `/api/v1/product-orders/${orderNo}`,
    method: 'get'
  })
}

// 支付订单
export function payOrder(orderNo, payMethod) {
  return request({
    url: `/api/v1/product-orders/${orderNo}/pay`,
    method: 'post',
    params: { payMethod }
  })
}

// 取消订单
export function cancelOrder(orderNo) {
  return request({
    url: `/api/v1/product-orders/${orderNo}/cancel`,
    method: 'post'
  })
}
```

---

### Task 13: 添加样式

**Files:**
- Modify: `frontend/src/views/member/Store.vue` (style section)

- [ ] **Step 1: 添加价格展示相关样式**

```css
/* 价格区域 */
.price-section {
  margin: 12px 0;
  padding: 12px;
  background: linear-gradient(135deg, #FFF8F0 0%, #FFF0E5 100%);
  border-radius: 10px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-bottom: 4px;
}

.discount-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.discount-tag {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.discount-amount {
  color: #FF6B35;
  font-weight: 600;
  font-size: 14px;
}

.final-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-label {
  font-size: 12px;
  color: #666;
}

.price-value {
  font-size: 22px;
  font-weight: 700;
  color: #FF6B35;
  font-family: 'Outfit', sans-serif;
}

.points-needed {
  font-size: 12px;
  color: #999;
}

/* 购买按钮 */
.buy-btn {
  width: 100%;
  border-radius: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
}

/* 订单弹窗样式 */
.order-content {
  padding: 8px;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.quantity-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.points-section {
  background: #F8F9FA;
  padding: 16px;
  border-radius: 8px;
  margin: 12px 0;
}

.points-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.points-available {
  color: #FF6B35;
  font-weight: 500;
}

.points-input {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 14px;
}

.pay-method-section {
  margin-top: 16px;
}

.detail-row.total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #ddd;
  font-size: 16px;
  font-weight: 600;
}

.detail-row.total .final {
  color: #FF6B35;
  font-size: 24px;
}
```

---

## 第五阶段：管理端页面更新

### Task 14: 更新管理端商品管理页面

**Files:**
- Modify: `frontend/src/views/admin/Products.vue`

- [ ] **Step 1: 修改商品表单 - 添加积分抵扣配置**

```vue
<el-row :gutter="20">
  <el-col :span="8">
    <el-form-item label="售价" prop="price">
      <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
    </el-form-item>
  </el-col>
  <el-col :span="8">
    <el-form-item label="库存" prop="stock">
      <el-input-number v-model="form.stock" :min="0" :precision="0" style="width: 100%" />
    </el-form-item>
  </el-col>
  <el-col :span="8">
    <el-form-item label="排序" prop="sort">
      <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
    </el-form-item>
  </el-col>
</el-row>

<!-- 积分抵扣配置 -->
<el-divider content-position="left">积分抵扣配置</el-divider>
<el-row :gutter="20">
  <el-col :span="8">
    <el-form-item label="抵扣类型">
      <el-select v-model="form.pointsDiscountType" placeholder="请选择" style="width: 100%">
        <el-option label="固定金额" value="FIXED" />
        <el-option label="比例抵扣" value="PERCENT" />
        <el-option label="不支持" value="NONE" />
      </el-select>
    </el-form-item>
  </el-col>
  <el-col :span="8">
    <el-form-item label="抵扣值">
      <el-input-number 
        v-model="form.pointsDiscountValue" 
        :min="0" 
        :precision="form.pointsDiscountType === 'PERCENT' ? 0 : 2" 
        style="width: 100%" 
      />
      <span class="form-tip">{{ form.pointsDiscountType === 'PERCENT' ? '%' : '元' }}</span>
    </el-form-item>
  </el-col>
  <el-col :span="8">
    <el-form-item label="最大抵扣">
      <el-input-number v-model="form.maxPointsDiscount" :min="0" :precision="2" style="width: 100%" />
    </el-form-item>
  </el-col>
</el-row>
```

---

### Task 15: 更新管理端订单管理页面

**Files:**
- Modify: `frontend/src/views/admin/OrderManagement.vue`

- [ ] **Step 1: 修改商品订单表格列**

```vue
<el-table :data="productOrderData" v-loading="loading" stripe>
  <el-table-column type="index" width="50" />
  <el-table-column prop="orderNo" label="订单号" width="160" />
  <el-table-column label="会员信息" min-width="150">
    <template #default="{ row }">
      <div class="member-info">
        <el-avatar :size="32" :src="row.memberAvatar">{{ row.memberName?.charAt(0) }}</el-avatar>
        <div class="member-detail">
          <div class="member-name">{{ row.memberName }}</div>
          <div class="member-phone">{{ row.memberPhone }}</div>
        </div>
      </div>
    </template>
  </el-table-column>
  <el-table-column label="商品" min-width="150">
    <template #default="{ row }">
      <div class="product-info">
        <div class="product-name">{{ row.productName }}</div>
        <div class="product-quantity">x{{ row.quantity }}</div>
      </div>
    </template>
  </el-table-column>
  <el-table-column label="原价" width="100">
    <template #default="{ row }">
      <span style="text-decoration: line-through; color: #999;">¥{{ row.originalPrice * row.quantity }}</span>
    </template>
  </el-table-column>
  <el-table-column label="积分抵扣" width="100">
    <template #default="{ row }">
      <span v-if="row.pointsDiscount > 0" style="color: #FF6B35;">-¥{{ row.pointsDiscount }}</span>
      <span v-else>-</span>
    </template>
  </el-table-column>
  <el-table-column label="实付金额" width="100">
    <template #default="{ row }">
      <span style="color: #FF6B35; font-weight: 600;">¥{{ row.payAmount }}</span>
    </template>
  </el-table-column>
  <el-table-column prop="payMethod" label="支付方式" width="100">
    <template #default="{ row }">
      <el-tag size="small">{{ row.payMethod }}</el-tag>
    </template>
  </el-table-column>
  <el-table-column prop="createTime" label="下单时间" width="160" />
  <el-table-column prop="status" label="状态" width="100">
    <template #default="{ row }">
      <el-tag :type="getProductOrderStatusType(row.status)">{{ getProductOrderStatusLabel(row.status) }}</el-tag>
    </template>
  </el-table-column>
  <el-table-column label="操作" width="180" fixed="right">
    <template #default="{ row }">
      <el-button type="primary" link @click="handleViewProductOrder(row)">
        <el-icon><View /></el-icon>查看
      </el-button>
      <el-button v-if="row.status === 'PENDING'" type="success" link @click="handleConfirmProductOrder(row)">
        <el-icon><Check /></el-icon>确认
      </el-button>
      <el-button v-if="row.status === 'PAID'" type="warning" link @click="handleShipProductOrder(row)">
        <el-icon><Box /></el-icon>发货
      </el-button>
    </template>
  </el-table-column>
</el-table>
```

---

## 实施检查清单

### 前端检查项
- [ ] 所有"积分商城"文本已替换为"购物中心"
- [ ] 商品卡片显示原价、积分抵扣、最终价格
- [ ] 购买弹窗支持积分抵扣滑块
- [ ] 订单记录显示完整的支付明细
- [ ] API调用正常，错误处理完善

### 后端检查项
- [ ] 数据库迁移脚本执行成功
- [ ] Entity、DTO、VO类创建完成
- [ ] Mapper接口创建完成
- [ ] Service实现业务逻辑
- [ ] Controller接口测试通过
- [ ] 错误码已添加

### 集成测试项
- [ ] 商品列表展示正常
- [ ] 价格计算准确
- [ ] 订单创建成功
- [ ] 积分扣减正确
- [ ] 库存扣减正确
- [ ] 订单状态流转正常
