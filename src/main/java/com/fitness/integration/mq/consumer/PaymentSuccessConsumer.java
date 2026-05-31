package com.fitness.integration.mq.consumer;

import com.fitness.config.RabbitMQConfig;
import com.fitness.integration.mq.BaseMqConsumer;
import com.fitness.integration.mq.message.PaymentSuccessMessage;
import com.fitness.modules.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSuccessConsumer extends BaseMqConsumer {

    private final OrderService orderService;

    @RabbitListener(id = "payment-success-consumer", queues = RabbitMQConfig.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(PaymentSuccessMessage message, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Received payment success message: orderId={}, orderNo={}, orderType={}",
                message.getOrderId(), message.getOrderNo(), message.getOrderType());
        try {
            orderService.handlePostPaidProcess(message.getOrderId());
            safeAck(channel, deliveryTag);
        } catch (Exception e) {
            log.error("Failed to handle post-paid logic: orderId={}, orderNo={}",
                    message.getOrderId(), message.getOrderNo(), e);
            safeNack(channel, deliveryTag, false);
        }
    }
}