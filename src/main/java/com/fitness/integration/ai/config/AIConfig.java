package com.fitness.integration.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Alibaba 配置类
 * 配置 ChatClient 及相关参数
 */
@Configuration
public class AIConfig {

    /**
     * 配置 ChatClient
     * 使用默认的 ChatClient.Builder 创建 ChatClient 实例
     * 参数通过 application.yml 中的 spring.ai.alibaba.chat.options 配置
     *
     * @param builder ChatClient 构建器
     * @return 配置好的 ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
