# Spring AI Alibaba HTTP 404 错误解决记录

## 问题描述

在集成 Spring AI Alibaba 1.1.2.0 与阿里云 DashScope 大模型服务时，调用 AI 对话接口出现以下错误：

```
java.lang.RuntimeException: AI 服务调用失败: HTTP 404 - No response body available
    at com.fitness.integration.ai.service.impl.AIServiceImpl.chat(AIServiceImpl.java:47)
```

## 环境信息

- **Spring Boot**: 3.5.0
- **Spring AI Alibaba**: 1.1.2.0
- **JDK**: 17
- **模型**: 阿里云 DashScope (通义千问)

## 问题排查过程

### 1. 初步排查 - 模型名称问题

首先怀疑是模型名称配置错误。在阿里云控制台看到的模型名称是 `qwen3-5-plus`，但 API 调用时需要使用映射后的名称。

**尝试修改模型名称：**
- `qwen-plus` - 正确的 API 调用名称
- `qwen-turbo` - 对应 qwen3-5-flash

**结果**：仍然 404 错误，问题不在模型名称。

### 2. 深入排查 - base-url 配置问题

检查 `application.yml` 配置：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:sk-xxx}
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1  # 问题在这里！
      chat:
        enabled: true
        options:
          model: qwen-plus
          temperature: 0.7
```

**问题分析：**
- 配置了 `base-url: https://dashscope.aliyuncs.com/compatible-mode/v1`
- Spring AI 会自动追加 `/v1/chat/completions`
- 最终请求路径变成：`/v1/v1/chat/completions`
- 导致 HTTP 404 错误

### 3. 解决方案

**移除 `base-url` 配置**，让 Spring AI Alibaba 使用默认端点。

## 正确配置

### application.yml

```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:your-api-key}
      chat:
        enabled: true
        options:
          model: ${AI_DASHSCOPE_MODEL:qwen-plus}
          temperature: 0.7
```

### AIConfig.java

```java
package com.fitness.integration.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AIConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
```

## 模型名称映射表

| 阿里云控制台显示名称 | API 调用名称 | 说明 |
|-------------------|-------------|------|
| qwen3-5-plus | qwen-plus | 推荐日常使用 |
| qwen3-5-flash | qwen-turbo | 快速响应 |
| gme-5 | - | 多模态模型 |

## 关键要点

1. **不要配置 `base-url`**：Spring AI Alibaba 会自动使用正确的默认端点
2. **使用正确的模型名称**：控制台显示名称 ≠ API 调用名称
3. **使用自动配置的 ChatModel**：简化配置，避免手动创建 Bean

## 参考文档

- [阿里云 DashScope 文档](https://help.aliyun.com/zh/dashscope/)
- [Spring AI Alibaba GitHub](https://github.com/alibaba/spring-ai-alibaba)

## 修复时间

2026-04-01
