package com.fitness.integration.payment.controller;

import com.fitness.config.RabbitMQConfig;
import com.fitness.integration.mq.MqMessageSender;
import com.fitness.integration.mq.message.PaymentSuccessMessage;
import com.fitness.modules.order.model.entity.Order;
import com.fitness.modules.order.model.enums.OrderStatusEnum;
import com.fitness.modules.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment/alipay")
@RequiredArgsConstructor
public class AlipayNotifyController {

    private final OrderService orderService;
    private final MqMessageSender mqMessageSender;

    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        log.info("收到支付宝异步通知");

        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(key, valueStr);
        }

        log.info("支付宝通知参数: out_trade_no={}, trade_no={}, trade_status={}, total_amount={}",
                params.get("out_trade_no"), params.get("trade_no"), params.get("trade_status"),
                params.get("total_amount"));

        try {
            Order order = orderService.markOrderPaid(params);
            if (order != null && OrderStatusEnum.PAID.getCode().equals(order.getStatus())) {
                PaymentSuccessMessage message = new PaymentSuccessMessage(
                        order.getId(), order.getOrderNo(), order.getOrderType());
                mqMessageSender.send(RabbitMQConfig.PAYMENT_EXCHANGE,
                        RabbitMQConfig.PAYMENT_SUCCESS_ROUTING_KEY, message);
                log.info("支付成功消息已发送到MQ: orderNo={}", order.getOrderNo());
            }
            return "success";
        } catch (Exception e) {
            log.error("处理支付宝通知失败: errorMsg={}", e.getMessage(), e);
            return "fail";
        }
    }
}