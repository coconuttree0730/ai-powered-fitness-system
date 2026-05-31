package com.fitness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String ORDER_DELAYED_EXCHANGE = "fitness.order.delayed";
    public static final String ORDER_TIMEOUT_QUEUE = "fitness.order.timeout.queue";
    public static final String ORDER_TIMEOUT_DLX = "fitness.order.timeout.dlx";
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

    public static final String PAYMENT_EXCHANGE = "fitness.payment.exchange";
    public static final String PAYMENT_SUCCESS_QUEUE = "fitness.payment.success.queue";
    public static final String PAYMENT_SUCCESS_DLX = "fitness.payment.success.dlx";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";

    public static final String AI_EXCHANGE = "fitness.ai.exchange";
    public static final String AI_PLAN_GENERATION_QUEUE = "fitness.ai.plan.generation.queue";
    public static final String AI_PLAN_GENERATION_DLX = "fitness.ai.plan.generation.dlx";
    public static final String AI_PLAN_GENERATION_ROUTING_KEY = "ai.plan.generate";

    public static final String NOTIFICATION_EXCHANGE = "fitness.notification.exchange";
    public static final String NOTIFICATION_SMS_QUEUE = "fitness.notification.sms.queue";
    public static final String NOTIFICATION_SMS_ROUTING_KEY = "notification.sms";

    public static final String EVENT_EXCHANGE = "fitness.event.exchange";
    public static final String EVENT_BOOKING_QUEUE = "fitness.event.booking.queue";
    public static final String EVENT_RANKING_QUEUE = "fitness.event.ranking.queue";
    public static final String EVENT_BOOKING_ROUTING_KEY = "event.booking.#";
    public static final String EVENT_RANKING_ROUTING_KEY = "event.ranking.#";

    private static final String X_DELAYED_MESSAGE_TYPE = "x-delayed-message";

    public static final Map<String, Object> DEFAULT_DLX_ARGS = Map.of(
            "x-dead-letter-exchange", "",
            "x-dead-letter-routing-key", ""
    );

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack && correlationData != null) {
                log.error("Message failed to reach exchange: id={}, cause={}",
                        correlationData.getId(), cause);
            }
        });

        template.setReturnsCallback(returned -> {
            log.error("Message returned: exchange={}, routingKey={}, replyCode={}, replyText={}",
                    returned.getExchange(), returned.getRoutingKey(),
                    returned.getReplyCode(), returned.getReplyText());
        });

        return template;
    }

    @Bean
    public CustomExchange orderDelayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(ORDER_DELAYED_EXCHANGE, X_DELAYED_MESSAGE_TYPE, true, false, args);
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(ORDER_TIMEOUT_DLX)
                .build();
    }

    @Bean
    public Queue orderTimeoutDlxQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_DLX).build();
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderDelayedExchange())
                .with(ORDER_TIMEOUT_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(PAYMENT_SUCCESS_DLX)
                .build();
    }

    @Bean
    public Queue paymentSuccessDlxQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_DLX).build();
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue())
                .to(paymentExchange())
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public DirectExchange aiExchange() {
        return new DirectExchange(AI_EXCHANGE, true, false);
    }

    @Bean
    public Queue aiPlanGenerationQueue() {
        return QueueBuilder.durable(AI_PLAN_GENERATION_QUEUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(AI_PLAN_GENERATION_DLX)
                .build();
    }

    @Bean
    public Queue aiPlanGenerationDlxQueue() {
        return QueueBuilder.durable(AI_PLAN_GENERATION_DLX).build();
    }

    @Bean
    public Binding aiPlanGenerationBinding() {
        return BindingBuilder.bind(aiPlanGenerationQueue())
                .to(aiExchange())
                .with(AI_PLAN_GENERATION_ROUTING_KEY);
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationSmsQueue() {
        return QueueBuilder.durable(NOTIFICATION_SMS_QUEUE).build();
    }

    @Bean
    public Binding notificationSmsBinding() {
        return BindingBuilder.bind(notificationSmsQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_SMS_ROUTING_KEY);
    }

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue eventBookingQueue() {
        return QueueBuilder.durable(EVENT_BOOKING_QUEUE).build();
    }

    @Bean
    public Queue eventRankingQueue() {
        return QueueBuilder.durable(EVENT_RANKING_QUEUE).build();
    }

    @Bean
    public Binding eventBookingBinding() {
        return BindingBuilder.bind(eventBookingQueue())
                .to(eventExchange())
                .with(EVENT_BOOKING_ROUTING_KEY);
    }

    @Bean
    public Binding eventRankingBinding() {
        return BindingBuilder.bind(eventRankingQueue())
                .to(eventExchange())
                .with(EVENT_RANKING_ROUTING_KEY);
    }
}