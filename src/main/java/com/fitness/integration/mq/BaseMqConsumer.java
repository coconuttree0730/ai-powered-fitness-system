package com.fitness.integration.mq;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;

/**
 * 消费者基类
 */
@Slf4j
public abstract class BaseMqConsumer {

    protected void safeAck(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("Failed to ACK message: deliveryTag={}", deliveryTag, e);
        }
    }

    protected void safeNack(Channel channel, long deliveryTag, boolean requeue) {
        try {
            channel.basicNack(deliveryTag, false, requeue);
        } catch (IOException e) {
            log.error("Failed to NACK message: deliveryTag={}", deliveryTag, e);
        }
    }
}