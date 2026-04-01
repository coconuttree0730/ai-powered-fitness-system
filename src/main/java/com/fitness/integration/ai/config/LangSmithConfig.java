package com.fitness.integration.ai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LangSmith 配置属性类
 * 用于从 application.yml 加载 LangSmith 相关配置
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "langsmith")
public class LangSmithConfig {

    /**
     * 是否启用 LangSmith
     */
    private boolean enabled = true;

    /**
     * LangSmith API Key
     */
    private String apiKey;

    /**
     * LangSmith 服务端点
     */
    private String endpoint = "https://api.smith.langchain.com";

    /**
     * 项目名称
     */
    private String projectName = "ai-fitness-system";

    /**
     * 是否启用追踪
     */
    private boolean tracingEnabled = true;

    /**
     * 检查 LangSmith 是否可用
     */
    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }
}
