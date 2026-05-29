package com.fitness.modules.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.order.mapper.OrderMapper;
import com.fitness.modules.order.mapper.ProductOrderExtMapper;
import com.fitness.modules.order.model.dto.OrderDTO;
import com.fitness.modules.order.model.entity.Order;
import com.fitness.modules.order.model.entity.ProductOrderExt;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.order.service.OrderService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder>
        implements ProductOrderService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String PICKUP_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int PICKUP_CODE_LENGTH = 6;
    private static final String STATUS_NOT_PICKED = "NOT_PICKED";

    private static final BigDecimal POINTS_TO_MONEY_RATE = new BigDecimal("0.01");

    private final ProductOrderMapper productOrderMapper;
    private final OrderMapper orderMapper;
    private final ProductOrderExtMapper productOrderExtMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final OrderService orderService;

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

        BigDecimal pointsDiscount = BigDecimal.ZERO;
        Integer usePoints = dto.getUsePoints() != null ? dto.getUsePoints() : 0;

        if (usePoints > 0 && "FIXED".equals(product.getPointsDiscountType())) {
            BigDecimal maxDiscount = product.getMaxPointsDiscount() != null ? product.getMaxPointsDiscount()
                    : totalOriginalPrice;
            BigDecimal requestedDiscount = new BigDecimal(usePoints).multiply(POINTS_TO_MONEY_RATE);
            pointsDiscount = requestedDiscount.min(maxDiscount).min(totalOriginalPrice);
        } else if (usePoints > 0 && "PERCENT".equals(product.getPointsDiscountType())) {
            BigDecimal percent = product.getPointsDiscountValue() != null ? product.getPointsDiscountValue()
                    : BigDecimal.ZERO;
            BigDecimal maxDiscount = totalOriginalPrice.multiply(percent).divide(new BigDecimal("100"), 2,
                    RoundingMode.HALF_UP);
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
    @CacheEvict(value = RedisCacheNames.PRODUCT_PUBLIC_LIST, allEntries = true)
    public ProductOrderVO createOrder(ProductOrderDTO dto, Long userId) {
        log.debug("创建商品订单: userId={}, productId={}, quantity={}",
                userId, dto.getProductId(), dto.getQuantity());

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        if (!"ACTIVE".equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        if (product.getStock() != null && product.getStock() < dto.getQuantity()) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType("PRODUCT");
        orderDTO.setProductId(dto.getProductId());
        orderDTO.setQuantity(dto.getQuantity());
        orderDTO.setUsePoints(dto.getUsePoints());
        orderDTO.setRemark(dto.getRemark());

        OrderVO orderVO = orderService.createOrder(orderDTO, userId);

        log.info("创建商品订单成功: orderNo={}, userId={}, productId={}",
                orderVO.getOrderNo(), userId, dto.getProductId());

        return convertOrderVOToProductOrderVO(orderVO);
    }

    @Override
    public ProductOrderVO getOrderDetail(String orderNo) {
        return getOrderDetail(orderNo, null);
    }

    @Override
    public ProductOrderVO getOrderDetail(String orderNo, Long userId) {
        OrderVO orderVO = userId == null
                ? orderService.getOrderDetail(orderNo)
                : orderService.getOrderDetail(orderNo, userId);
        return convertOrderVOToProductOrderVO(orderVO);
    }

    @Override
    public List<ProductOrderVO> getUserOrders(Long userId) {
        List<OrderVO> orders = orderService.getUserOrders(userId);
        return orders.stream()
                .filter(vo -> "PRODUCT".equals(vo.getOrderType()))
                .map(this::convertOrderVOToProductOrderVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisCacheNames.PRODUCT_PUBLIC_LIST, allEntries = true)
    public void cancelOrder(String orderNo, Long userId) {
        orderService.cancelOrder(orderNo, userId);
        log.info("取消商品订单: orderNo={}, userId={}", orderNo, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo, String trackingNo, String carrier) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        ensureProductOrder(order);

        if (!STATUS_NOT_PICKED.equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        }

        order.setStatus("SHIPPED");
        orderMapper.updateById(order);

        ProductOrderExt ext = productOrderExtMapper.selectByOrderId(order.getId());
        if (ext != null) {
            ext.setTrackingNo(trackingNo);
            ext.setCarrier(carrier);
            productOrderExtMapper.updateById(ext);
        }

        log.info("订单发货: orderNo={}, trackingNo={}", orderNo, trackingNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        ensureProductOrder(order);

        order.setStatus("COMPLETED");
        orderMapper.updateById(order);

        log.info("订单完成: orderNo={}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPickup(String orderNo, String pickupCode) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        ensureProductOrder(order);

        if (!STATUS_NOT_PICKED.equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAID);
        }

        ProductOrderExt ext = productOrderExtMapper.selectByOrderId(order.getId());
        if (ext == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "商品订单扩展信息不存在");
        }
        if (pickupCode != null && !pickupCode.isBlank()
                && ext.getPickupCode() != null && !pickupCode.equals(ext.getPickupCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "取货码不正确");
        }

        order.setStatus("COMPLETED");
        orderMapper.updateById(order);

        ext.setPickupStatus("PICKED");
        ext.setPickupTime(LocalDateTime.now());
        productOrderExtMapper.updateById(ext);

        log.info("确认取货成功: orderNo={}, pickupCode={}", orderNo, pickupCode);
    }

    @Override
    public List<ProductOrderVO> getAllOrders(String status, String keyword) {
        List<Order> orders = orderMapper.selectByType("PRODUCT", status, keyword);
        return orders.stream().map(this::convertUnifiedOrderToVO).collect(Collectors.toList());
    }

    // ==================== 辅助方法 ====================

    private String generatePickupCode() {
        StringBuilder code = new StringBuilder(PICKUP_CODE_LENGTH);
        for (int i = 0; i < PICKUP_CODE_LENGTH; i++) {
            code.append(PICKUP_CODE_CHARS.charAt(SECURE_RANDOM.nextInt(PICKUP_CODE_CHARS.length())));
        }
        return code.toString();
    }

    private ProductOrderVO convertToVO(ProductOrder order) {
        ProductOrderVO vo = new ProductOrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setStatusLabel(getStatusLabel(order.getStatus()));
        vo.setPayMethodLabel(getPayMethodLabel(order.getPayMethod()));

        if ("PENDING".equals(order.getStatus()) && order.getExpireTime() != null) {
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), order.getExpireTime());
            vo.setRemainingSeconds(Math.max(0, seconds));
        }

        return vo;
    }

    private ProductOrderVO convertOrderVOToProductOrderVO(OrderVO orderVO) {
        ProductOrderVO vo = new ProductOrderVO();
        vo.setId(orderVO.getId());
        vo.setOrderNo(orderVO.getOrderNo());
        vo.setPayAmount(orderVO.getPayAmount());
        vo.setPayMethod(orderVO.getPayMethod());
        vo.setPayMethodLabel(orderVO.getPayMethodLabel());
        vo.setPayTime(orderVO.getPayTime());
        vo.setStatus(orderVO.getStatus());
        vo.setStatusLabel(orderVO.getStatusLabel());
        vo.setAlipayTradeNo(orderVO.getAlipayTradeNo());
        vo.setRemark(orderVO.getRemark());
        vo.setCreatedAt(orderVO.getCreateTime());

        if (orderVO.getProductExt() != null) {
            ProductOrderExt ext = orderVO.getProductExt();
            vo.setProductId(ext.getProductId());
            vo.setProductName(ext.getProductName());
            vo.setQuantity(ext.getQuantity());
            vo.setPointsUsed(ext.getPointsUsed());
            vo.setPointsDiscount(ext.getPointsDiscount());
            vo.setOriginalPrice(orderVO.getOriginalAmount());
            vo.setTrackingNo(ext.getTrackingNo());
            vo.setCarrier(ext.getCarrier());
            vo.setAddress(ext.getAddress());
            vo.setPickupType(ext.getPickupType());
            vo.setPickupCode(ext.getPickupCode());
            vo.setPickupStatus(ext.getPickupStatus());
            vo.setPickupStatusLabel(getPickupStatusLabel(ext.getPickupStatus()));
            vo.setPickupTime(ext.getPickupTime());
        }

        return vo;
    }

    private ProductOrderVO convertUnifiedOrderToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setStatusLabel(getStatusLabel(order.getStatus()));
        vo.setPayMethodLabel(getPayMethodLabel(order.getPayMethod()));
        vo.setProductExt(productOrderExtMapper.selectByOrderId(order.getId()));
        return convertOrderVOToProductOrderVO(vo);
    }

    private void ensureProductOrder(Order order) {
        if (!"PRODUCT".equals(order.getOrderType())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "非商品订单");
        }
    }

    private String getStatusLabel(String status) {
        return switch (status) {
            case "PENDING" -> "待支付";
            case "PAID" -> "已支付";
            case "NOT_PICKED" -> "待取货";
            case "PROCESSING" -> "处理中";
            case "SHIPPED" -> "已发货";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            case "TIMEOUT" -> "已超时";
            default -> status;
        };
    }

    private String getPayMethodLabel(String payMethod) {
        if (payMethod == null)
            return null;
        return switch (payMethod) {
            case "ALIPAY" -> "支付宝";
            case "BALANCE" -> "余额支付";
            default -> payMethod;
        };
    }

    private String getPickupStatusLabel(String pickupStatus) {
        if (pickupStatus == null)
            return null;
        return switch (pickupStatus) {
            case "NOT_PICKED" -> "未取货";
            case "PICKED" -> "已取货";
            default -> pickupStatus;
        };
    }
}
