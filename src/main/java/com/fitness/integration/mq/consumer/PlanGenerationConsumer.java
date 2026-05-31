package com.fitness.integration.mq.consumer;

import com.fitness.config.RabbitMQConfig;
import com.fitness.integration.mq.BaseMqConsumer;
import com.fitness.integration.mq.message.PlanGenerationMessage;
import com.fitness.modules.plan.service.FitnessPlanService;
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
public class PlanGenerationConsumer extends BaseMqConsumer {

    private final FitnessPlanService fitnessPlanService;

    @RabbitListener(id = "plan-generation-consumer", queues = RabbitMQConfig.AI_PLAN_GENERATION_QUEUE, concurrency = "1")
    public void handlePlanGeneration(PlanGenerationMessage message, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Received plan generation message: userId={}, planId={}", message.getUserId(), message.getPlanId());
        try {
            fitnessPlanService.processPlanGeneration(message.getPlanId(), message.getUserId());
            safeAck(channel, deliveryTag);
        } catch (Exception e) {
            log.error("Failed to generate plan: userId={}, planId={}", message.getUserId(), message.getPlanId(), e);
            safeNack(channel, deliveryTag, false);
        }
    }
}