package com.fitness.integration.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
/**
 * 发送器基类
 */
@Component
@RequiredArgsConstructor
public class MqMessageSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * 
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     */
    public void send(String exchange, String routingKey, Object message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
        log.debug("Sent message: exchange={}, routingKey={}, correlationId={}",
                exchange, routingKey, correlationData.getId());
    }

    /**
     * 发送延迟消息
     * 
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     * @param delayMs    延迟时间（毫秒）
     */
    public void sendDelayed(String exchange, String routingKey, Object message, long delayMs) {
        MessagePostProcessor postProcessor = msg -> {
            MessageProperties props = msg.getMessageProperties();
            props.setDelayLong(delayMs);
            return msg;
        };
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(exchange, routingKey, message, postProcessor, correlationData);
        log.debug("Sent delayed message: exchange={}, routingKey={}, delayMs={}, correlationId={}",
                exchange, routingKey, delayMs, correlationData.getId());
    }
}