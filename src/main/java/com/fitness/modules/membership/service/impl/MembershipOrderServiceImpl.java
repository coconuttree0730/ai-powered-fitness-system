package com.fitness.modules.membership.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.integration.payment.service.AlipayService;
import com.fitness.modules.membership.mapper.MembershipCardMapper;
import com.fitness.modules.membership.mapper.MembershipOrderMapper;
import com.fitness.modules.membership.model.dto.MembershipOrderDTO;
import com.fitness.modules.membership.model.entity.MembershipCard;
import com.fitness.modules.membership.model.entity.MembershipOrder;
import com.fitness.modules.membership.model.vo.CardWalletVO;
import com.fitness.modules.membership.model.vo.MembershipOrderVO;
import com.fitness.modules.membership.model.vo.UserMembershipVO;
import com.fitness.modules.membership.service.MembershipOrderService;
import com.fitness.modules.membership.service.UserMembershipService;
import com.fitness.modules.order.mapper.MembershipOrderExtMapper;
import com.fitness.modules.order.mapper.OrderMapper;
import com.fitness.modules.order.model.dto.OrderDTO;
import com.fitness.modules.order.model.entity.MembershipOrderExt;
import com.fitness.modules.order.model.entity.Order;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.order.service.OrderService;
import com.fitness.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipOrderServiceImpl extends ServiceImpl<MembershipOrderMapper, MembershipOrder>
        implements MembershipOrderService {

    private final MembershipOrderMapper orderMapper;
    private final MembershipCardMapper cardMapper;
    private final UserMapper userMapper;
    private final AlipayService alipayService;
    private final UserMembershipService userMembershipService;
    private final OrderService orderService;
    private final OrderMapper unifiedOrderMapper;
    private final MembershipOrderExtMapper membershipOrderExtMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MembershipOrderVO createOrder(MembershipOrderDTO dto, Long userId) {
        log.debug("开始创建会员卡订单: userId={}, cardId={}", userId, dto.getCardId());

        MembershipCard card = cardMapper.selectById(dto.getCardId());
        if (card == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡不存在");
        }
        if (!"ACTIVE".equals(card.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE, "会员卡已下架");
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType("MEMBERSHIP");
        orderDTO.setCardId(dto.getCardId());
        orderDTO.setRemark(dto.getRemark());

        OrderVO orderVO = orderService.createOrder(orderDTO, userId);

        log.info("创建会员卡订单成功: orderNo={}, userId={}, cardId={}",
                orderVO.getOrderNo(), userId, dto.getCardId());

        return convertOrderVOToMembershipOrderVO(orderVO);
    }

    @Override
    public MembershipOrderVO getOrderDetail(String orderNo) {
        return getOrderDetail(orderNo, null);
    }

    @Override
    public MembershipOrderVO getOrderDetail(String orderNo, Long userId) {
        OrderVO orderVO = userId == null
                ? orderService.getOrderDetail(orderNo)
                : orderService.getOrderDetail(orderNo, userId);
        return convertOrderVOToMembershipOrderVO(orderVO);
    }

    @Override
    public List<MembershipOrderVO> getUserOrders(Long userId) {
        List<OrderVO> orders = orderService.getUserOrders(userId);
        return orders.stream()
                .filter(vo -> "MEMBERSHIP".equals(vo.getOrderType()))
                .map(this::convertOrderVOToMembershipOrderVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        orderService.cancelOrder(orderNo, userId);
        log.info("取消会员卡订单: orderNo={}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateOrder(String orderNo, Long userId) {
        Order order = unifiedOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态不正确，无法激活");
        }
        if (!"MEMBERSHIP".equals(order.getOrderType())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "非会员卡订单，无法激活");
        }

        order.setStatus("ACTIVATED");
        unifiedOrderMapper.updateById(order);

        MembershipOrderExt ext = membershipOrderExtMapper.selectByOrderId(order.getId());
        if (ext == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "会员卡订单扩展信息不存在");
        }

        MembershipCard card = cardMapper.selectById(ext.getCardId());

        MembershipOrder mo = new MembershipOrder();
        mo.setUserId(order.getUserId());
        mo.setCardId(card != null ? card.getId() : null);
        mo.setCardName(card != null ? card.getName() : null);
        mo.setStatus(order.getStatus());

        userMembershipService.activateMembership(mo);

        log.info("会员卡激活成功: orderNo={}, userId={}", orderNo, userId);
    }

    @Override
    public CardWalletVO getCardWallet(Long userId) {
        CardWalletVO vo = new CardWalletVO();

        UserMembershipVO membership = userMembershipService.getUserMembership(userId);
        if (membership != null && Boolean.TRUE.equals(membership.getIsActive())) {
            vo.setWalletStatus("ACTIVATED");
            vo.setMembership(membership);
            return vo;
        }

        Order paidOrder = unifiedOrderMapper.selectLatestPaidOrderByType(userId, "MEMBERSHIP");
        if (paidOrder != null) {
            vo.setWalletStatus("PAID_UNACTIVATED");
            CardWalletVO.PaidOrderItem item = new CardWalletVO.PaidOrderItem();
            item.setOrderNo(paidOrder.getOrderNo());
            item.setPrice(paidOrder.getPayAmount().toPlainString());

            MembershipOrderExt ext = membershipOrderExtMapper.selectByOrderId(paidOrder.getId());
            if (ext != null) {
                item.setCardName(ext.getCardName() != null ? ext.getCardName() : "");
                MembershipCard card = cardMapper.selectById(ext.getCardId());
                if (card != null) {
                    item.setDurationDays(card.getDurationDays());
                }
            }
            vo.setPaidOrder(item);
            return vo;
        }

        Order pendingOrder = unifiedOrderMapper.selectLatestPendingOrderByType(userId, "MEMBERSHIP");
        if (pendingOrder != null) {
            String alipayStatus = alipayService.queryOrderStatus(pendingOrder.getOrderNo());
            if ("TRADE_SUCCESS".equals(alipayStatus) || "TRADE_FINISHED".equals(alipayStatus)) {
                log.info("补偿查询发现已支付订单: orderNo={}, 同步状态", pendingOrder.getOrderNo());
                pendingOrder.setStatus("PAID");
                pendingOrder.setPayMethod("ALIPAY");
                pendingOrder.setPayTime(LocalDateTime.now());

                MembershipOrderExt ext = membershipOrderExtMapper.selectByOrderId(pendingOrder.getId());
                if (ext != null) {
                    MembershipCard pendingCard = cardMapper.selectById(ext.getCardId());
                    if (pendingCard != null) {
                        ext.setExpireTime(LocalDateTime.now().plusDays(pendingCard.getDurationDays()));
                        membershipOrderExtMapper.updateById(ext);
                        if (pendingCard.getPointsReward() != null && pendingCard.getPointsReward() > 0) {
                            userMapper.addPoints(userId, pendingCard.getPointsReward());
                        }
                    }
                }
                unifiedOrderMapper.updateById(pendingOrder);

                vo.setWalletStatus("PAID_UNACTIVATED");
                CardWalletVO.PaidOrderItem item = new CardWalletVO.PaidOrderItem();
                item.setOrderNo(pendingOrder.getOrderNo());
                item.setPrice(pendingOrder.getPayAmount().toPlainString());
                if (ext != null) {
                    item.setCardName(ext.getCardName() != null ? ext.getCardName() : "");
                    MembershipCard pendingCard = cardMapper.selectById(ext.getCardId());
                    if (pendingCard != null) {
                        item.setDurationDays(pendingCard.getDurationDays());
                    }
                }
                vo.setPaidOrder(item);
                return vo;
            }

            vo.setWalletStatus("PENDING_ORDER");
            CardWalletVO.PendingOrderItem item = new CardWalletVO.PendingOrderItem();
            item.setOrderNo(pendingOrder.getOrderNo());
            item.setPrice(pendingOrder.getPayAmount().toPlainString());

            MembershipOrderExt ext = membershipOrderExtMapper.selectByOrderId(pendingOrder.getId());
            if (ext != null) {
                item.setCardName(ext.getCardName() != null ? ext.getCardName() : "");
            }
            vo.setPendingOrder(item);
            return vo;
        }

        vo.setWalletStatus("NONE");
        return vo;
    }

    // ==================== 辅助方法 ====================

    private MembershipOrderVO convertOrderVOToMembershipOrderVO(OrderVO orderVO) {
        MembershipOrderVO vo = new MembershipOrderVO();
        vo.setId(orderVO.getId());
        vo.setOrderNo(orderVO.getOrderNo());
        vo.setPrice(orderVO.getOriginalAmount());
        vo.setPayAmount(orderVO.getPayAmount());
        vo.setPayMethod(orderVO.getPayMethod());
        vo.setPayMethodLabel(orderVO.getPayMethodLabel());
        vo.setPayTime(orderVO.getPayTime());
        vo.setStatus(orderVO.getStatus());
        vo.setStatusLabel(orderVO.getStatusLabel());
        vo.setRemark(orderVO.getRemark());
        vo.setCreateTime(orderVO.getCreateTime());
        vo.setUpdateTime(orderVO.getUpdateTime());

        if (orderVO.getMembershipExt() != null) {
            MembershipOrderExt ext = orderVO.getMembershipExt();
            vo.setCardId(ext.getCardId());
            vo.setCardName(ext.getCardName());
            vo.setExpireTime(ext.getExpireTime());
        }

        return vo;
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
            case "ACTIVATED" -> "已激活";
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
}
