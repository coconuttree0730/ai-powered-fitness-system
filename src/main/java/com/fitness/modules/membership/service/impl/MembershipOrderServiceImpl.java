package com.fitness.modules.membership.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.integration.payment.service.AlipayService;
import com.fitness.modules.membership.mapper.MembershipCardMapper;
import com.fitness.modules.membership.mapper.MembershipOrderMapper;
import com.fitness.modules.membership.model.dto.MembershipOrderDTO;
import com.fitness.modules.membership.model.dto.PayOrderDTO;
import com.fitness.modules.membership.model.entity.MembershipCard;
import com.fitness.modules.membership.model.entity.MembershipOrder;
import com.fitness.modules.membership.model.vo.AlipayPayVO;
import com.fitness.modules.membership.model.vo.MembershipOrderVO;
import com.fitness.modules.membership.service.MembershipOrderService;
import com.fitness.modules.membership.service.UserMembershipService;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipOrderServiceImpl extends ServiceImpl<MembershipOrderMapper, MembershipOrder> implements MembershipOrderService {

    private final MembershipOrderMapper orderMapper;
    private final MembershipCardMapper cardMapper;
    private final UserMapper userMapper;
    private final AlipayService alipayService;
    private final UserMembershipService userMembershipService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long ORDER_TIMEOUT_MINUTES = 30;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MembershipOrderVO createOrder(MembershipOrderDTO dto, Long userId) {
        log.debug("开始创建会员卡订单: userId={}, cardId={}", userId, dto.getCardId());
        
        // 验证会员卡是否存在且上架
        MembershipCard card = cardMapper.selectById(dto.getCardId());
        log.debug("查询会员卡结果: cardId={}, card={}", dto.getCardId(), card);
        
        if (card == null) {
            log.warn("会员卡不存在: cardId={}", dto.getCardId());
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡不存在");
        }
        if (!"ACTIVE".equals(card.getStatus())) {
            log.warn("会员卡已下架: cardId={}, status={}", dto.getCardId(), card.getStatus());
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE, "会员卡已下架");
        }

        // 创建订单
        MembershipOrder order = new MembershipOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setCardId(dto.getCardId());
        order.setCardName(card.getName());
        order.setPrice(card.getPrice());
        order.setPayAmount(card.getPrice());
        order.setStatus("PENDING");
        order.setRemark(dto.getRemark());
        
        log.debug("准备保存订单: orderNo={}, userId={}, cardId={}, price={}", 
                order.getOrderNo(), userId, dto.getCardId(), order.getPrice());

        save(order);
        
        log.debug("订单保存成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
        log.info("创建会员卡订单成功: orderNo={}, userId={}, cardId={}", order.getOrderNo(), userId, dto.getCardId());

        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlipayPayVO payOrder(PayOrderDTO dto) {
        log.debug("开始处理订单支付: orderNo={}, payMethod={}", dto.getOrderNo(), dto.getPayMethod());
        
        MembershipOrder order = orderMapper.selectByOrderNo(dto.getOrderNo());
        log.debug("查询订单结果: orderNo={}, order={}", dto.getOrderNo(), order);
        
        if (order == null) {
            log.warn("订单不存在: orderNo={}", dto.getOrderNo());
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }

        if (!"PENDING".equals(order.getStatus())) {
            log.warn("订单状态不正确: orderNo={}, status={}", dto.getOrderNo(), order.getStatus());
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态不正确");
        }

        // 检查订单是否超时
        long minutesElapsed = ChronoUnit.MINUTES.between(order.getCreateTime(), LocalDateTime.now());
        log.debug("订单创建时间: createTime={}, 已过去{}分钟", order.getCreateTime(), minutesElapsed);
        
        if (isOrderTimeout(order)) {
            log.warn("订单已超时: orderNo={}, 已过去{}分钟", dto.getOrderNo(), minutesElapsed);
            cancelOrder(order.getOrderNo(), order.getUserId());
            throw new BusinessException(ErrorCode.ORDER_TIMEOUT, "订单已超时，请重新下单");
        }

        AlipayPayVO payVO = new AlipayPayVO();
        payVO.setOrderNo(order.getOrderNo());

        if ("ALIPAY".equals(dto.getPayMethod())) {
            log.debug("使用支付宝支付: orderNo={}, amount={}", order.getOrderNo(), order.getPayAmount());
            // 调用支付宝支付
            String payForm = alipayService.createPayOrder(
                    order.getOrderNo(),
                    order.getPayAmount(),
                    "购买会员卡：" + order.getCardName(),
                    order.getRemark()
            );
            payVO.setPayForm(payForm);
            log.debug("支付宝支付表单生成成功: orderNo={}", order.getOrderNo());
        } else if ("BALANCE".equals(dto.getPayMethod())) {
            log.debug("使用余额支付: orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
            // 余额支付
            handleBalancePay(order);
        } else {
            log.warn("不支持的支付方式: orderNo={}, payMethod={}", dto.getOrderNo(), dto.getPayMethod());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的支付方式");
        }

        log.info("订单支付处理完成: orderNo={}, payMethod={}", dto.getOrderNo(), dto.getPayMethod());
        return payVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayCallback(Map<String, String> params) {
        log.info("处理支付宝回调: {}", params);
        log.debug("支付宝回调参数详情: out_trade_no={}, trade_no={}, trade_status={}, total_amount={}, buyer_logon_id={}",
                params.get("out_trade_no"), params.get("trade_no"), params.get("trade_status"),
                params.get("total_amount"), params.get("buyer_logon_id"));

        // 验证签名
        log.debug("开始验证支付宝回调签名...");
        if (!alipayService.verifyNotify(params)) {
            log.error("支付宝回调验签失败: params={}", params);
            throw new BusinessException(ErrorCode.SIGNATURE_ERROR, "验签失败");
        }
        log.debug("支付宝回调验签成功");

        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        String totalAmount = params.get("total_amount");
        
        log.debug("解析回调参数: orderNo={}, tradeNo={}, tradeStatus={}, totalAmount={}", 
                orderNo, tradeNo, tradeStatus, totalAmount);

        MembershipOrder order = orderMapper.selectByOrderNo(orderNo);
        log.debug("查询订单结果: orderNo={}, order={}", orderNo, order);
        
        if (order == null) {
            log.error("支付宝回调订单不存在: {}", orderNo);
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }

        if (!"PENDING".equals(order.getStatus())) {
            log.warn("订单状态不是待支付，无需处理: orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }

        // 校验金额是否一致
        if (totalAmount != null) {
            BigDecimal callbackAmount = new BigDecimal(totalAmount);
            if (callbackAmount.compareTo(order.getPayAmount()) != 0) {
                log.error("订单金额不一致: orderNo={}, orderAmount={}, callbackAmount={}", 
                        orderNo, order.getPayAmount(), callbackAmount);
                throw new BusinessException(ErrorCode.PARAM_ERROR, "订单金额不一致");
            }
            log.debug("订单金额校验通过: orderNo={}, amount={}", orderNo, totalAmount);
        }

        // 处理支付成功
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            log.debug("处理支付成功: orderNo={}, tradeStatus={}", orderNo, tradeStatus);
            
            order.setStatus("PAID");
            order.setPayMethod("ALIPAY");
            order.setPayTime(LocalDateTime.now());
            order.setAlipayTradeNo(tradeNo);

            // 计算会员到期时间
            MembershipCard card = cardMapper.selectById(order.getCardId());
            log.debug("查询会员卡信息: cardId={}, card={}", order.getCardId(), card);
            
            if (card != null) {
                order.setExpireTime(LocalDateTime.now().plusDays(card.getDurationDays()));
                log.debug("设置会员到期时间: orderNo={}, expireTime={}", orderNo, order.getExpireTime());

                // 赠送积分（原子操作，防止并发丢失更新）
                if (card.getPointsReward() != null && card.getPointsReward() > 0) {
                    userMapper.addPoints(order.getUserId(), card.getPointsReward());
                    log.info("用户购买会员卡获得积分: userId={}, pointsReward={}",
                            order.getUserId(), card.getPointsReward());
                }
            }

            updateById(order);
            log.debug("订单状态更新成功: orderNo={}, status=PAID", orderNo);

            // 激活会员
            log.debug("开始激活会员: userId={}, cardId={}", order.getUserId(), order.getCardId());
            userMembershipService.activateMembership(order);

            log.info("支付宝支付成功，订单已处理: orderNo={}, tradeNo={}, userId={}", orderNo, tradeNo, order.getUserId());
        } else {
            log.warn("支付宝回调状态非成功: orderNo={}, tradeStatus={}", orderNo, tradeStatus);
        }
    }

    @Override
    public MembershipOrderVO getOrderDetail(String orderNo) {
        MembershipOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return convertToVO(order);
    }

    @Override
    public List<MembershipOrderVO> getUserOrders(Long userId) {
        List<MembershipOrder> orders = orderMapper.selectByUserId(userId);
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        MembershipOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态不正确，无法取消");
        }

        order.setStatus("CANCELLED");
        updateById(order);

        log.info("取消会员卡订单: orderNo={}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeoutOrders() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
        List<MembershipOrder> timeoutOrders = orderMapper.selectTimeoutOrders(timeoutTime);

        for (MembershipOrder order : timeoutOrders) {
            order.setStatus("TIMEOUT");
            updateById(order);
            log.info("订单超时自动取消: orderNo={}", order.getOrderNo());
        }
    }

    @Override
    public MembershipOrder selectByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    private void handleBalancePay(MembershipOrder order) {
        // 原子CAS扣减余额，防止并发超扣
        int affected = userMapper.deductBalance(order.getUserId(), order.getPayAmount());
        if (affected == 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, "余额不足");
        }

        // 更新订单状态
        order.setStatus("PAID");
        order.setPayMethod("BALANCE");
        order.setPayTime(LocalDateTime.now());

        // 计算会员到期时间
        MembershipCard card = cardMapper.selectById(order.getCardId());
        if (card != null) {
            order.setExpireTime(LocalDateTime.now().plusDays(card.getDurationDays()));

            // 赠送积分（原子操作）
            if (card.getPointsReward() != null && card.getPointsReward() > 0) {
                userMapper.addPoints(order.getUserId(), card.getPointsReward());
            }
        }

        updateById(order);

        // 激活会员
        userMembershipService.activateMembership(order);

        log.info("余额支付成功: orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
    }

    private boolean isOrderTimeout(MembershipOrder order) {
        return ChronoUnit.MINUTES.between(order.getCreateTime(), LocalDateTime.now()) > ORDER_TIMEOUT_MINUTES;
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        return "MS" + dateStr + randomStr;
    }

    private MembershipOrderVO convertToVO(MembershipOrder order) {
        MembershipOrderVO vo = new MembershipOrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setStatusLabel(getStatusLabel(order.getStatus()));
        vo.setPayMethodLabel(getPayMethodLabel(order.getPayMethod()));
        return vo;
    }

    private String getStatusLabel(String status) {
        return switch (status) {
            case "PENDING" -> "待支付";
            case "PAID" -> "已支付";
            case "CANCELLED" -> "已取消";
            case "TIMEOUT" -> "已超时";
            default -> status;
        };
    }

    private String getPayMethodLabel(String payMethod) {
        if (payMethod == null) return null;
        return switch (payMethod) {
            case "ALIPAY" -> "支付宝";
            case "BALANCE" -> "余额支付";
            default -> payMethod;
        };
    }
}
