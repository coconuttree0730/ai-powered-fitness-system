package com.fitness.modules.chat.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JianXiaoZhuAgentPromptsTest {

    @Test
    void systemPromptShouldMentionToolPriority() {
        JianXiaoZhuAgentPrompts prompts = new JianXiaoZhuAgentPrompts();
        String systemPrompt = prompts.systemPrompt();

        assertTrue(systemPrompt.contains("调用工具") || systemPrompt.contains("伐工具"));
    }
}
