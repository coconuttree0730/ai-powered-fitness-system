package com.fitness.modules.membership.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.membership.model.dto.MembershipOrderDTO;
import com.fitness.modules.membership.model.entity.MembershipOrder;
import com.fitness.modules.membership.model.vo.CardWalletVO;
import com.fitness.modules.membership.model.vo.MembershipOrderVO;

import java.util.List;

public interface MembershipOrderService extends IService<MembershipOrder> {

    MembershipOrderVO createOrder(MembershipOrderDTO dto, Long userId);

    MembershipOrderVO getOrderDetail(String orderNo);

    MembershipOrderVO getOrderDetail(String orderNo, Long userId);

    List<MembershipOrderVO> getUserOrders(Long userId);

    void cancelOrder(String orderNo, Long userId);

    void activateOrder(String orderNo, Long userId);

    CardWalletVO getCardWallet(Long userId);
}
