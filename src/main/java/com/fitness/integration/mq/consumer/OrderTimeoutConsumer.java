package com.fitness.integration.mq.consumer;

import com.fitness.config.RabbitMQConfig;
import com.fitness.integration.mq.BaseMqConsumer;
import com.fitness.integration.mq.message.OrderTimeoutMessage;
import com.fitness.modules.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer extends BaseMqConsumer {

    private final OrderService orderService;

    @RabbitListener(id = "order-timeout-consumer", queues = RabbitMQConfig.ORDER_TIMEOUT_QUEUE)
    public void handleOrderTimeout(OrderTimeoutMessage message, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Received order timeout message: orderId={}, orderNo={}", message.getOrderId(), message.getOrderNo());
        try {
            orderService.handleTimeoutOrder(message.getOrderId(), message.getOrderNo());
            safeAck(channel, deliveryTag);
        } catch (Exception e) {
            log.error("Failed to handle order timeout: orderId={}, orderNo={}", message.getOrderId(),
                    message.getOrderNo(), e);
            safeNack(channel, deliveryTag, true);
        }
    }
}