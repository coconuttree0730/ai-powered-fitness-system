package com.fitness.modules.chat.prompt;

import com.fitness.integration.ai.prompt.AiPromptSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatPromptTemplatesTest {

    private final ChatPromptTemplates promptTemplates = new ChatPromptTemplates();

    @Test
    void createRagPromptShouldSeparateSystemAndUserContent() {
        AiPromptSpec prompt = promptTemplates.createRagPrompt("营业时间？", "周一到周日 06:00-22:00");

        assertFalse(prompt.system().isBlank());
        assertTrue(prompt.system().contains("06:00-22:00"));
        assertTrue(prompt.user().contains("营业时间"));
    }

    @Test
    void buildAgentUserPromptShouldIncludeMemoryBlockWhenPresent() {
        String prompt = promptTemplates.buildAgentUserPrompt("帮我看看课程", "用户偏好：增肌");

        assertTrue(prompt.contains("用户长期记忆"));
        assertTrue(prompt.contains("用户偏好：增肌"));
        assertTrue(prompt.contains("帮我看看课程"));
    }
}
