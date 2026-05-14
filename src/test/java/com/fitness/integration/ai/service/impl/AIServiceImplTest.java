package com.fitness.integration.ai.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import com.fitness.integration.ai.prompt.AiPromptSpec;
import com.fitness.integration.ai.prompt.PromptTemplates;
import com.fitness.integration.ai.support.DashScopeChatOptionsFactory;
import com.fitness.integration.ai.support.AiFitnessPlanValidator;
import com.fitness.integration.ai.support.AiResponseSanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.converter.StructuredOutputConverter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceImplTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClientRequestSpec requestSpec;

    @Mock
    private CallResponseSpec callResponseSpec;

    @Mock
    private PromptTemplates promptTemplates;

    @Mock
    private AiResponseSanitizer aiResponseSanitizer;

    @Mock
    private AiFitnessPlanValidator aiFitnessPlanValidator;

    @Mock
    private DashScopeChatOptionsFactory dashScopeChatOptionsFactory;

    @InjectMocks
    private AIServiceImpl aiService;

    @Test
    void generateFitnessPlanFromProfileShouldPreferStructuredOutputConverter() {
        Map<String, Object> profile = Map.of("goal", "muscle gain");
        AiPromptSpec prompt = new AiPromptSpec("system prompt", "user prompt");
        FitnessPlanResponseDTO expected = new FitnessPlanResponseDTO();
        DashScopeChatOptions options = DashScopeChatOptions.builder().build();

        when(promptTemplates.createFitnessPlanPrompt(profile)).thenReturn(prompt);
        when(dashScopeChatOptionsFactory.structuredJsonOutputOptions()).thenReturn(options);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.options(eq(options))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(StructuredOutputConverter.class))).thenReturn(expected);

        FitnessPlanResponseDTO result = aiService.generateFitnessPlanFromProfile(profile);

        assertSame(expected, result);
        verify(callResponseSpec).entity(any(StructuredOutputConverter.class));
        verify(requestSpec).options(eq(options));
        verify(aiFitnessPlanValidator).validateFitnessPlan(expected);
        verify(callResponseSpec, never()).content();
        verify(aiResponseSanitizer, never()).cleanJsonResponse(anyString());
    }

    @Test
    void generateFitnessPlanFromProfileShouldFallbackToManualJsonParsingWhenStructuredOutputFails() {
        Map<String, Object> profile = Map.of("goal", "fat loss");
        AiPromptSpec prompt = new AiPromptSpec("system prompt", "user prompt");
        String rawResponse = "```json\n{\"subtitle\":\"fallback\",\"weeklyPlan\":[]}\n```";
        String cleanedResponse = "{\"subtitle\":\"fallback\",\"weeklyPlan\":[]}";
        DashScopeChatOptions options = DashScopeChatOptions.builder().build();

        when(promptTemplates.createFitnessPlanPrompt(profile)).thenReturn(prompt);
        when(dashScopeChatOptionsFactory.structuredJsonOutputOptions()).thenReturn(options);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.options(eq(options))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(StructuredOutputConverter.class)))
                .thenThrow(new IllegalArgumentException("structured output failed"));
        when(callResponseSpec.content()).thenReturn(rawResponse);
        when(aiResponseSanitizer.cleanJsonResponse(rawResponse)).thenReturn(cleanedResponse);

        FitnessPlanResponseDTO result = aiService.generateFitnessPlanFromProfile(profile);

        assertNotNull(result);
        verify(callResponseSpec).entity(any(StructuredOutputConverter.class));
        verify(requestSpec).options(eq(options));
        verify(callResponseSpec).content();
        verify(aiResponseSanitizer).cleanJsonResponse(rawResponse);
        verify(aiFitnessPlanValidator).validateFitnessPlan(result);
    }
}
