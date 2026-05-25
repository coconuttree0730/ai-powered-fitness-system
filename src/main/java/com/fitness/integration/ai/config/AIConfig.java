package com.fitness.integration.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Alibaba 配置类
 * 配置 ChatClient 及相关参数
 * 使用 Spring AI 自动配置的 ChatModel
 */
@Slf4j
@Configuration
public class AIConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.chat.options.model}")
    private String model;

    /**
     * 配置 ChatClient
     * 使用 Spring AI 自动配置的 ChatModel
     *
     * @param chatModel 自动配置的 ChatModel（由 spring-ai-alibaba-starter-dashscope 提供）
     * @return 配置好的 ChatClient
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        log.info("=== Spring AI Alibaba 配置信息 ===");
        log.info("API Key configured: {}", apiKey != null ? "YES" : "MISSING");
        log.info("Model: {}", model);
        log.info("================================");

        return ChatClient.builder(chatModel);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}
