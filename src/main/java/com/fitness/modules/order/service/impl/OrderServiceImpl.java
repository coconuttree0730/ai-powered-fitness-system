package com.fitness.modules.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.integration.payment.service.AlipayService;
import com.fitness.modules.coach.mapper.CoachPackageMapper;
import com.fitness.modules.coach.model.entity.CoachPackage;
import com.fitness.modules.membership.mapper.MembershipCardMapper;
import com.fitness.modules.membership.model.entity.MembershipCard;
import com.fitness.modules.membership.model.vo.AlipayPayVO;
import com.fitness.modules.order.mapper.CoachPackageOrderExtMapper;
import com.fitness.modules.order.mapper.MembershipOrderExtMapper;
import com.fitness.modules.order.mapper.OrderMapper;
import com.fitness.modules.order.mapper.ProductOrderExtMapper;
import com.fitness.modules.order.model.enums.OrderStatusEnum;
import com.fitness.modules.order.model.enums.OrderTypeEnum;
import com.fitness.modules.order.model.enums.PayMethodEnum;
import com.fitness.modules.order.model.dto.OrderDTO;
import com.fitness.modules.order.model.entity.CoachPackageOrderExt;
import com.fitness.modules.order.model.entity.MembershipOrderExt;
import com.fitness.modules.order.model.entity.Order;
import com.fitness.modules.order.model.entity.ProductOrderExt;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.order.service.OrderService;
import com.fitness.modules.product.mapper.ProductMapper;
import com.fitness.modules.product.model.entity.Product;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.service.CoachStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long ORDER_TIMEOUT_MINUTES = 30;

    private static final String PICKUP_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int PICKUP_CODE_LENGTH = 6;

    private static final BigDecimal POINTS_TO_MONEY_RATE = new BigDecimal("0.01");

    private final OrderMapper orderMapper;
    private final ProductOrderExtMapper productOrderExtMapper;
    private final CoachPackageOrderExtMapper coachPackageOrderExtMapper;
    private final MembershipOrderExtMapper membershipOrderExtMapper;
    private final AlipayService alipayService;
    private final ProductMapper productMapper;
    private final CoachPackageMapper coachPackageMapper;
    private final MembershipCardMapper membershipCardMapper;
    private final UserMapper userMapper;
    private final CoachStudentService coachStudentService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplateCacheSupport redisTemplateCacheSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderDTO dto, Long userId) {
        log.debug("创建订单: userId={}, orderType={}", userId, dto.getOrderType());

        if (OrderTypeEnum.PRODUCT.getCode().equals(dto.getOrderType())) {
            return createProductOrder(dto, userId);
        } else if (OrderTypeEnum.COACH_PACKAGE.getCode().equals(dto.getOrderType())) {
            return createCoachPackageOrder(dto, userId);
        } else if (OrderTypeEnum.MEMBERSHIP.getCode().equals(dto.getOrderType())) {
            return createMembershipOrder(dto, userId);
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的订单类型: " + dto.getOrderType());
        }
    }

    private OrderVO createProductOrder(OrderDTO dto, Long userId) {
        if (dto.getProductId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品ID不能为空");
        }
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!"ACTIVE".equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }

        int quantity = dto.getQuantity() != null ? dto.getQuantity() : 1;

        String stockKey = "fitness:product:stock:reserved:" + dto.getProductId();
        Duration stockTtl = Duration.ofMinutes(ORDER_TIMEOUT_MINUTES + 5);
        Integer stockToInit = product.getStock();
        if (stockToInit != null) {
            stringRedisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(stockToInit), stockTtl);
            Long reserved = stringRedisTemplate.opsForValue().decrement(stockKey, quantity);
            if (reserved == null || reserved < 0) {
                stringRedisTemplate.opsForValue().increment(stockKey, quantity);
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
            }
        }

        if (product.getStock() != null && product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }
        BigDecimal originalAmount = product.getOriginalPrice().multiply(new BigDecimal(quantity));

        BigDecimal pointsDiscount = BigDecimal.ZERO;
        Integer actualUsePoints = 0;

        int usePoints = dto.getUsePoints() != null ? dto.getUsePoints() : 0;
        if (usePoints > 0) {
            User user = userMapper.selectById(userId);
            int userPoints = user.getPoints() != null ? user.getPoints() : 0;

            if ("FIXED".equals(product.getPointsDiscountType())) {
                BigDecimal maxDiscount = product.getMaxPointsDiscount() != null
                        ? product.getMaxPointsDiscount()
                        : originalAmount;
                BigDecimal requestedDiscount = new BigDecimal(usePoints).multiply(POINTS_TO_MONEY_RATE);
                pointsDiscount = requestedDiscount.min(maxDiscount).min(originalAmount);
            } else if ("PERCENT".equals(product.getPointsDiscountType())) {
                BigDecimal percent = product.getPointsDiscountValue() != null
                        ? product.getPointsDiscountValue()
                        : BigDecimal.ZERO;
                BigDecimal maxDiscount = originalAmount.multiply(percent)
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                BigDecimal requestedDiscount = new BigDecimal(usePoints).multiply(POINTS_TO_MONEY_RATE);
                pointsDiscount = requestedDiscount.min(maxDiscount).min(originalAmount);
            }

            actualUsePoints = pointsDiscount.divide(POINTS_TO_MONEY_RATE, 0, java.math.RoundingMode.CEILING)
                    .intValue();

            if (actualUsePoints > 0) {
                int deducted = userMapper.deductPoints(userId, actualUsePoints);
                if (deducted <= 0) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "积分不足或并发操作冲突，请重试");
                }
            }
        }

        BigDecimal payAmount = originalAmount.subtract(pointsDiscount);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType(OrderTypeEnum.PRODUCT.getCode());
        order.setOriginalAmount(originalAmount);
        order.setDiscountAmount(pointsDiscount);
        order.setPayAmount(payAmount);
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        order.setRemark(dto.getRemark());
        save(order);

        ProductOrderExt ext = new ProductOrderExt();
        ext.setOrderId(order.getId());
        ext.setProductId(product.getId());
        ext.setProductName(product.getName());
        ext.setQuantity(quantity);
        ext.setPointsUsed(actualUsePoints);
        ext.setPointsDiscount(pointsDiscount);
        ext.setPickupStatus(null);
        productOrderExtMapper.insert(ext);

        log.info("创建商品订单成功: orderNo={}, userId={}, productId={}, originalAmount={}, pointsDiscount={}, payAmount={}",
                order.getOrderNo(), userId, product.getId(), originalAmount, pointsDiscount, payAmount);
        return convertToVO(order);
    }

    private OrderVO createCoachPackageOrder(OrderDTO dto, Long userId) {
        if (dto.getCoachPackageId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "教练套餐ID不能为空");
        }
        CoachPackage pkg = coachPackageMapper.selectById(dto.getCoachPackageId());
        if (pkg == null) {
            throw new BusinessException(ErrorCode.COACH_PACKAGE_NOT_FOUND);
        }
        if (!"ACTIVE".equals(pkg.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE, "教练套餐已下架");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType(OrderTypeEnum.COACH_PACKAGE.getCode());
        order.setOriginalAmount(pkg.getOriginalPrice());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(pkg.getOriginalPrice());
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        order.setRemark(dto.getRemark());
        save(order);

        CoachPackageOrderExt ext = new CoachPackageOrderExt();
        ext.setOrderId(order.getId());
        ext.setCoachPackageId(pkg.getId());
        ext.setCoachId(pkg.getCoachId());
        ext.setPackageName(pkg.getName());
        ext.setExpireTime(LocalDateTime.now().plusMinutes(ORDER_TIMEOUT_MINUTES));
        coachPackageOrderExtMapper.insert(ext);

        log.info("创建教练套餐订单成功: orderNo={}, userId={}, coachPackageId={}, payAmount={}",
                order.getOrderNo(), userId, pkg.getId(), order.getPayAmount());
        return convertToVO(order);
    }

    private OrderVO createMembershipOrder(OrderDTO dto, Long userId) {
        if (dto.getCardId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会员卡ID不能为空");
        }
        MembershipCard card = membershipCardMapper.selectById(dto.getCardId());
        if (card == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡不存在");
        }
        if (!"ACTIVE".equals(card.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE, "会员卡已下架");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType(OrderTypeEnum.MEMBERSHIP.getCode());
        order.setOriginalAmount(card.getPrice());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(card.getPrice());
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        order.setRemark(dto.getRemark());
        save(order);

        MembershipOrderExt ext = new MembershipOrderExt();
        ext.setOrderId(order.getId());
        ext.setCardId(card.getId());
        ext.setCardName(card.getName());
        membershipOrderExtMapper.insert(ext);

        log.info("创建会员卡订单成功: orderNo={}, userId={}, cardId={}, payAmount={}",
                order.getOrderNo(), userId, card.getId(), order.getPayAmount());
        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlipayPayVO payOrder(String orderNo, String payMethod) {
        return payOrder(orderNo, payMethod, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlipayPayVO payOrder(String orderNo, String payMethod, Long userId) {
        log.info("开始支付: orderNo={}, payMethod={}", orderNo, payMethod);

        if (!PayMethodEnum.ALIPAY.getCode().equals(payMethod)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_PAY_METHOD);
        }

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }

        if (!OrderStatusEnum.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        }

        if (isOrderTimeout(order)) {
            log.warn("订单已超时: orderNo={}", orderNo);
            order.setStatus(OrderStatusEnum.TIMEOUT.getCode());
            updateById(order);
            throw new BusinessException(ErrorCode.ORDER_TIMEOUT);
        }

        String subject = buildPaySubject(order);
        log.info("支付参数: orderNo={}, amount={}, subject={}, orderType={}, remark={}",
                order.getOrderNo(), order.getPayAmount(), subject, order.getOrderType(), order.getRemark());

        String payForm = alipayService.createPayOrder(
                order.getOrderNo(),
                order.getPayAmount(),
                subject,
                order.getRemark());

        AlipayPayVO payVO = new AlipayPayVO();
        payVO.setOrderNo(order.getOrderNo());
        payVO.setPayForm(payForm);

        log.info("支付请求处理成功: orderNo={}, payMethod={}", orderNo, payMethod);
        return payVO;
    }

    private String buildPaySubject(Order order) {
        if (OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())) {
            ProductOrderExt ext = productOrderExtMapper.selectByOrderId(order.getId());
            return ext != null ? "购买商品：" + ext.getProductName() : "购买商品";
        }
        if (OrderTypeEnum.COACH_PACKAGE.getCode().equals(order.getOrderType())) {
            CoachPackageOrderExt ext = coachPackageOrderExtMapper.selectByOrderId(order.getId());
            return ext != null ? "购买教练套餐：" + ext.getPackageName() : "购买教练套餐";
        }
        if (OrderTypeEnum.MEMBERSHIP.getCode().equals(order.getOrderType())) {
            MembershipOrderExt ext = membershipOrderExtMapper.selectByOrderId(order.getId());
            return ext != null ? "购买会员卡：" + ext.getCardName() : "购买会员卡";
        }
        return "订单支付：" + order.getOrderNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayCallback(Map<String, String> params) {
        log.info("处理订单支付宝回调: {}", params);

        if (!alipayService.verifyNotify(params)) {
            log.error("支付宝回调验签失败");
            throw new BusinessException(ErrorCode.SIGNATURE_ERROR);
        }

        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        String totalAmount = params.get("total_amount");

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.error("支付宝回调订单不存在: {}", orderNo);
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (!OrderStatusEnum.PENDING.getCode().equals(order.getStatus())) {
            log.warn("订单状态不是待支付，无需处理: orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }

        if (totalAmount != null) {
            BigDecimal callbackAmount = new BigDecimal(totalAmount);
            if (callbackAmount.compareTo(order.getPayAmount()) != 0) {
                log.error("订单金额不一致: orderNo={}, orderAmount={}, callbackAmount={}",
                        orderNo, order.getPayAmount(), callbackAmount);
                throw new BusinessException(ErrorCode.PARAM_ERROR, "订单金额不一致");
            }
        }

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            log.debug("处理支付成功: orderNo={}", orderNo);

            order.setPayMethod(PayMethodEnum.ALIPAY.getCode());
            order.setPayTime(LocalDateTime.now());
            order.setAlipayTradeNo(tradeNo);

            try {
                handlePostPaidLogic(order);
            } catch (BusinessException e) {
                log.error("支付后置处理失败，订单保持待处理: orderNo={}, error={}", orderNo, e.getMessage());
                return;
            }

            order.setStatus(OrderStatusEnum.PAID.getCode());
            updateById(order);

            log.info("订单支付宝支付成功: orderNo={}, tradeNo={}, userId={}", orderNo, tradeNo, order.getUserId());
        } else {
            log.warn("支付宝回调状态非成功: orderNo={}, tradeStatus={}", orderNo, tradeStatus);
        }
    }

    private void handlePostPaidLogic(Order order) {
        if (OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())) {
            handleProductPostPaid(order);
        } else if (OrderTypeEnum.COACH_PACKAGE.getCode().equals(order.getOrderType())) {
            handleCoachPackagePostPaid(order);
        } else if (OrderTypeEnum.MEMBERSHIP.getCode().equals(order.getOrderType())) {
            handleMembershipPostPaid(order);
        } else {
            log.warn("未知订单类型，跳过后置处理: orderType={}", order.getOrderType());
        }
    }

    private void handleProductPostPaid(Order order) {
        ProductOrderExt ext = productOrderExtMapper.selectByOrderId(order.getId());
        if (ext == null) {
            log.warn("商品订单扩展信息不存在: orderId={}", order.getId());
            return;
        }
        int affected = productMapper.decreaseStock(ext.getProductId(), ext.getQuantity());
        if (affected <= 0) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }
        releaseStockReservation(order);
        order.setStatus(OrderStatusEnum.NOT_PICKED.getCode());
        updateById(order);
        ext.setPickupCode(generatePickupCode());
        ext.setPickupStatus("NOT_PICKED");
        productOrderExtMapper.updateById(ext);
        log.info("商品订单支付后扣库存并生成取货码: orderNo={}, productId={}, quantity={}, pickupCode={}",
                order.getOrderNo(), ext.getProductId(), ext.getQuantity(), ext.getPickupCode());
    }

    private void handleCoachPackagePostPaid(Order order) {
        CoachPackageOrderExt ext = coachPackageOrderExtMapper.selectByOrderId(order.getId());
        if (ext == null) {
            log.warn("教练套餐订单扩展信息不存在: orderId={}", order.getId());
            return;
        }
        CoachPackage pkg = coachPackageMapper.selectById(ext.getCoachPackageId());
        try {
            coachStudentService.bindStudent(order.getUserId(), ext.getCoachId(),
                    ext.getCoachPackageId(),
                    pkg != null ? pkg.getPackageCode() : null,
                    pkg != null ? pkg.getValidityDays() : null);
        } catch (BusinessException e) {
            if (ErrorCode.COACH_ALREADY_BOUND.getCode().equals(e.getCode())) {
                log.warn("教练学员关系已存在，跳过绑定: coachId={}, userId={}, orderNo={}",
                        ext.getCoachId(), order.getUserId(), order.getOrderNo());
            } else {
                throw e;
            }
        }
        log.info("教练套餐支付后绑定教练: orderNo={}, coachId={}, userId={}",
                order.getOrderNo(), ext.getCoachId(), order.getUserId());
    }

    private void handleMembershipPostPaid(Order order) {
        MembershipOrderExt ext = membershipOrderExtMapper.selectByOrderId(order.getId());
        if (ext == null) {
            log.warn("会员卡订单扩展信息不存在: orderId={}", order.getId());
            return;
        }

        MembershipCard card = membershipCardMapper.selectById(ext.getCardId());
        if (card != null) {
            LocalDateTime expireTime = LocalDateTime.now().plusDays(card.getDurationDays());
            ext.setExpireTime(expireTime);
            membershipOrderExtMapper.updateById(ext);

            if (card.getPointsReward() != null && card.getPointsReward() > 0) {
                userMapper.addPoints(order.getUserId(), card.getPointsReward());
                log.info("用户购买会员卡获得积分: userId={}, pointsReward={}",
                        order.getUserId(), card.getPointsReward());
            }

            log.info("会员卡支付成功，等待用户手动激活: orderNo={}, userId={}",
                    order.getOrderNo(), order.getUserId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO getOrderDetail(String orderNo) {
        return getOrderDetail(orderNo, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO getOrderDetail(String orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此订单");
        }
        syncPaidOrderIfNeeded(order);
        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderVO> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.selectByUserId(userId);
        orders.forEach(this::syncPaidOrderIfNeeded);
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        Map<Long, ProductOrderExt> productExtMap = productOrderExtMapper.selectByOrderIds(orderIds)
                .stream().collect(Collectors.toMap(ProductOrderExt::getOrderId, e -> e));
        Map<Long, CoachPackageOrderExt> coachExtMap = coachPackageOrderExtMapper.selectByOrderIds(orderIds)
                .stream().collect(Collectors.toMap(CoachPackageOrderExt::getOrderId, e -> e));
        Map<Long, MembershipOrderExt> membershipExtMap = membershipOrderExtMapper.selectByOrderIds(orderIds)
                .stream().collect(Collectors.toMap(MembershipOrderExt::getOrderId, e -> e));

        Set<Long> coachIds = coachExtMap.values().stream()
                .map(CoachPackageOrderExt::getCoachId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, User> coachMap = coachIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(coachIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        return orders.stream()
                .map(order -> convertToVO(order, productExtMap, coachExtMap, membershipExtMap, coachMap))
                .collect(Collectors.toList());
    }

    private void syncPaidOrderIfNeeded(Order order) {
        if (order == null || !OrderStatusEnum.PENDING.getCode().equals(order.getStatus())) {
            return;
        }

        String alipayStatus;
        try {
            alipayStatus = alipayService.queryOrderStatus(order.getOrderNo());
        } catch (RuntimeException e) {
            log.warn("Query Alipay status failed, keep local order pending: orderNo={}",
                    order.getOrderNo(), e);
            return;
        }

        if (!"TRADE_SUCCESS".equals(alipayStatus) && !"TRADE_FINISHED".equals(alipayStatus)) {
            return;
        }

        log.info("Sync paid Alipay order: orderNo={}, alipayStatus={}",
                order.getOrderNo(), alipayStatus);

        try {
            handlePostPaidLogic(order);
        } catch (BusinessException e) {
            log.error("同步支付后后置处理失败，订单保持待处理: orderNo={}, error={}",
                    order.getOrderNo(), e.getMessage());
            return;
        }

        order.setStatus(OrderStatusEnum.PAID.getCode());
        order.setPayMethod(PayMethodEnum.ALIPAY.getCode());
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }

        if (!OrderStatusEnum.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        }

        refundProductPointsIfNeeded(order);
        releaseStockReservation(order);
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        updateById(order);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PRODUCT_PUBLIC_LIST);

        log.info("取消订单: orderNo={}, userId={}", orderNo, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeoutOrders() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
        List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(timeoutTime);

        for (Order order : timeoutOrders) {
            refundProductPointsIfNeeded(order);
            releaseStockReservation(order);
            order.setStatus(OrderStatusEnum.TIMEOUT.getCode());
            updateById(order);
            log.info("订单超时自动取消: orderNo={}", order.getOrderNo());
        }

        if (!timeoutOrders.isEmpty()) {
            redisTemplateCacheSupport.evictAll(RedisCacheNames.PRODUCT_PUBLIC_LIST);
            log.info("批量处理超时订单完成，共处理 {} 条", timeoutOrders.size());
        }
    }

    @Override
    public Order selectByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    private void refundProductPointsIfNeeded(Order order) {
        if (!OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())) {
            return;
        }
        ProductOrderExt ext = productOrderExtMapper.selectByOrderId(order.getId());
        if (ext != null && ext.getPointsUsed() != null && ext.getPointsUsed() > 0) {
            userMapper.addPoints(order.getUserId(), ext.getPointsUsed());
            log.info("退还商品订单积分: orderNo={}, userId={}, points={}",
                    order.getOrderNo(), order.getUserId(), ext.getPointsUsed());
        }
    }

    private void releaseStockReservation(Order order) {
        if (!OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())) {
            return;
        }
        ProductOrderExt ext = productOrderExtMapper.selectByOrderId(order.getId());
        if (ext != null && ext.getProductId() != null && ext.getQuantity() != null && ext.getQuantity() > 0) {
            String stockKey = "fitness:product:stock:reserved:" + ext.getProductId();
            stringRedisTemplate.opsForValue().increment(stockKey, ext.getQuantity());
        }
    }

    private boolean isOrderTimeout(Order order) {
        if (order.getCreateTime() == null) {
            return false;
        }
        return ChronoUnit.MINUTES.between(order.getCreateTime(), LocalDateTime.now()) > ORDER_TIMEOUT_MINUTES;
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        return "ORD" + dateStr + randomStr;
    }

    private String generatePickupCode() {
        StringBuilder code = new StringBuilder(PICKUP_CODE_LENGTH);
        for (int i = 0; i < PICKUP_CODE_LENGTH; i++) {
            code.append(PICKUP_CODE_CHARS.charAt(SECURE_RANDOM.nextInt(PICKUP_CODE_CHARS.length())));
        }
        return code.toString();
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setStatusLabel(getStatusLabel(order.getStatus()));
        vo.setPayMethodLabel(getPayMethodLabel(order.getPayMethod()));
        vo.setOrderTypeLabel(getOrderTypeLabel(order.getOrderType()));

        if (order.getOrderType() != null) {
            if (OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())) {
                vo.setProductExt(productOrderExtMapper.selectByOrderId(order.getId()));
            } else if (OrderTypeEnum.COACH_PACKAGE.getCode().equals(order.getOrderType())) {
                CoachPackageOrderExt ext = coachPackageOrderExtMapper.selectByOrderId(order.getId());
                if (ext != null && ext.getCoachId() != null) {
                    User coach = userMapper.selectById(ext.getCoachId());
                    if (coach != null) {
                        ext.setCoachName(coach.getUsername());
                        ext.setCoachAvatar(coach.getAvatar());
                    }
                }
                vo.setCoachPackageExt(ext);
            } else if (OrderTypeEnum.MEMBERSHIP.getCode().equals(order.getOrderType())) {
                vo.setMembershipExt(membershipOrderExtMapper.selectByOrderId(order.getId()));
            }
        }

        return vo;
    }

    private OrderVO convertToVO(Order order, Map<Long, ProductOrderExt> productExtMap,
            Map<Long, CoachPackageOrderExt> coachExtMap,
            Map<Long, MembershipOrderExt> membershipExtMap,
            Map<Long, User> coachMap) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setStatusLabel(getStatusLabel(order.getStatus()));
        vo.setPayMethodLabel(getPayMethodLabel(order.getPayMethod()));
        vo.setOrderTypeLabel(getOrderTypeLabel(order.getOrderType()));

        if (order.getOrderType() != null) {
            if (OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())) {
                vo.setProductExt(productExtMap.get(order.getId()));
            } else if (OrderTypeEnum.COACH_PACKAGE.getCode().equals(order.getOrderType())) {
                CoachPackageOrderExt ext = coachExtMap.get(order.getId());
                if (ext != null && ext.getCoachId() != null) {
                    User coach = coachMap.get(ext.getCoachId());
                    if (coach != null) {
                        ext.setCoachName(coach.getUsername());
                        ext.setCoachAvatar(coach.getAvatar());
                    }
                }
                vo.setCoachPackageExt(ext);
            } else if (OrderTypeEnum.MEMBERSHIP.getCode().equals(order.getOrderType())) {
                vo.setMembershipExt(membershipExtMap.get(order.getId()));
            }
        }

        return vo;
    }

    private String getStatusLabel(String status) {
        return OrderStatusEnum.getLabelByCode(status);
    }

    private String getPayMethodLabel(String payMethod) {
        return PayMethodEnum.getLabelByCode(payMethod);
    }

    private String getOrderTypeLabel(String orderType) {
        return OrderTypeEnum.getLabelByCode(orderType);
    }
}
