package com.fitness.modules.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
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
import com.fitness.modules.user.mapper.UserFitnessProfileMapper;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.model.entity.UserFitnessProfile;
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
    private final UserFitnessProfileMapper userFitnessProfileMapper;

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

        BigDecimal pointsDiscount = BigDecimal.ZERO;
        Integer usePoints = dto.getUsePoints() != null ? dto.getUsePoints() : 0;

        if (usePoints > 0 && "FIXED".equals(product.getPointsDiscountType())) {
            BigDecimal maxDiscount = product.getMaxPointsDiscount() != null ?
                product.getMaxPointsDiscount() : totalOriginalPrice;
            BigDecimal requestedDiscount = new BigDecimal(usePoints).multiply(POINTS_TO_MONEY_RATE);
            pointsDiscount = requestedDiscount.min(maxDiscount).min(totalOriginalPrice);
        } else if (usePoints > 0 && "PERCENT".equals(product.getPointsDiscountType())) {
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

        if (!"ACTIVE".equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }

        if (product.getStock() < dto.getQuantity()) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }

        if (product.getCoachId() != null) {
            UserFitnessProfile profile = userFitnessProfileMapper.selectByUserId(userId);
            if (profile != null && profile.getPrivateCoachId() != null
                    && !profile.getPrivateCoachId().equals(product.getCoachId())) {
                throw new BusinessException(ErrorCode.COACH_ALREADY_BOUND);
            }
        }

        CalculatePriceDTO calculateDTO = new CalculatePriceDTO();
        calculateDTO.setProductId(dto.getProductId());
        calculateDTO.setQuantity(dto.getQuantity());
        calculateDTO.setUsePoints(dto.getUsePoints());
        PriceCalculationVO priceCalc = calculatePrice(calculateDTO, userId);

        if (!priceCalc.getPointsSufficient()) {
            throw new BusinessException(ErrorCode.POINTS_INSUFFICIENT);
        }

        int affected = productMapper.decreaseStock(dto.getProductId(), dto.getQuantity());
        if (affected == 0) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }

        User user = userMapper.selectById(userId);
        user.setPoints(user.getPoints() - priceCalc.getUsePoints());
        userMapper.updateById(user);

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
        order.setCoachId(product.getCoachId());

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

        if (order.getCoachId() != null) {
            bindCoachToMember(order.getUserId(), order.getCoachId());
        }

        return convertToVO(order);
    }

    private void bindCoachToMember(Long userId, Long coachId) {
        UserFitnessProfile profile = userFitnessProfileMapper.selectByUserId(userId);
        if (profile == null) {
            profile = new UserFitnessProfile();
            profile.setUserId(userId);
            profile.setPrivateCoachId(coachId);
            userFitnessProfileMapper.insert(profile);
        } else {
            profile.setPrivateCoachId(coachId);
            userFitnessProfileMapper.updateById(profile);
        }
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

        productMapper.increaseStock(order.getProductId(), order.getQuantity());

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
