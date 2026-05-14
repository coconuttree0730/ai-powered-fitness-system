package com.fitness.integration.ai.prompt;

/**
 * 统一承载 system / user 双层提示词。
 */
public record AiPromptSpec(String system, String user) {
}
