package com.fitness.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.order.model.dto.OrderDTO;
import com.fitness.modules.order.model.entity.Order;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.membership.model.vo.AlipayPayVO;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    OrderVO createOrder(OrderDTO dto, Long userId);

    AlipayPayVO payOrder(String orderNo, String payMethod);

    AlipayPayVO payOrder(String orderNo, String payMethod, Long userId);

    void handleAlipayCallback(Map<String, String> params);

    OrderVO getOrderDetail(String orderNo);

    OrderVO getOrderDetail(String orderNo, Long userId);

    List<OrderVO> getUserOrders(Long userId);

    void cancelOrder(String orderNo, Long userId);

    void handleTimeoutOrders();

    Order selectByOrderNo(String orderNo);
}
