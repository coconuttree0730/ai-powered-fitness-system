package com.fitness.modules.membership.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.membership.model.dto.MembershipOrderDTO;
import com.fitness.modules.membership.model.dto.PayOrderDTO;
import com.fitness.modules.membership.model.entity.MembershipOrder;
import com.fitness.modules.membership.model.vo.AlipayPayVO;
import com.fitness.modules.membership.model.vo.MembershipOrderVO;

import java.util.List;
import java.util.Map;

public interface MembershipOrderService extends IService<MembershipOrder> {

    MembershipOrderVO createOrder(MembershipOrderDTO dto, Long userId);

    AlipayPayVO payOrder(PayOrderDTO dto);

    void handleAlipayCallback(Map<String, String> params);

    MembershipOrderVO getOrderDetail(String orderNo);

    List<MembershipOrderVO> getUserOrders(Long userId);

    void cancelOrder(String orderNo, Long userId);

    void handleTimeoutOrders();

    MembershipOrder selectByOrderNo(String orderNo);
}
