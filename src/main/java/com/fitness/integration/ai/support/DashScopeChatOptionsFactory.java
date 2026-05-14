package com.fitness.integration.ai.support;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.stereotype.Component;

@Component
public class DashScopeChatOptionsFactory {

    public DashScopeChatOptions structuredJsonOutputOptions() {
        return DashScopeChatOptions.builder()
                .responseFormat(DashScopeResponseFormat.builder()
                        .type(DashScopeResponseFormat.Type.JSON_OBJECT)
                        .build())
                .enableThinking(false)
                .build();
    }
}
