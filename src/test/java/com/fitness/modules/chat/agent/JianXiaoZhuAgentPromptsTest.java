package com.fitness.modules.chat.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JianXiaoZhuAgentPromptsTest {

    @Test
    void systemPromptShouldMentionToolPriority() {
        JianXiaoZhuAgentPrompts prompts = new JianXiaoZhuAgentPrompts();

        assertTrue(prompts.systemPrompt().contains("优先调用工具"));
    }
}
