package com.fitness.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.result.PageResult;
import com.fitness.modules.order.model.dto.OrderDTO;
import com.fitness.modules.order.model.entity.Order;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.membership.model.vo.AlipayPayVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    OrderVO createOrder(OrderDTO dto, Long userId);

    AlipayPayVO payOrder(String orderNo, String payMethod);

    AlipayPayVO payOrder(String orderNo, String payMethod, Long userId);

    void handleAlipayCallback(Map<String, String> params);

    Order markOrderPaid(Map<String, String> params);

    void handlePostPaidProcess(Long orderId);

    OrderVO getOrderDetail(String orderNo);

    OrderVO getOrderDetail(String orderNo, Long userId);

    List<OrderVO> getUserOrders(Long userId);

    void cancelOrder(String orderNo, Long userId);

    void handleTimeoutOrders();

    void handleTimeoutOrder(Long orderId, String orderNo);

    Order selectByOrderNo(String orderNo);

    PageResult<OrderVO> getAdminOrderPage(int page, int pageSize, String orderType,
                                          String status, String keyword,
                                          LocalDateTime startTime, LocalDateTime endTime);

    OrderVO getAdminOrderDetail(String orderNo);

    void confirmPayment(String orderNo);

    void shipOrder(String orderNo, String trackingNo, String carrier);

    Map<String, Object> getOrderStats();
}
