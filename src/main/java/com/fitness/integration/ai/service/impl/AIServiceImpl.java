package com.fitness.integration.ai.service.impl;

import com.fitness.integration.ai.exception.AiIntegrationException;
import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import com.fitness.integration.ai.prompt.AiPromptSpec;
import com.fitness.integration.ai.prompt.PromptTemplates;
import com.fitness.integration.ai.service.AIService;
import com.fitness.integration.ai.support.AiFitnessPlanValidator;
import com.fitness.integration.ai.support.AiResponseSanitizer;
import com.fitness.integration.ai.support.DashScopeChatOptionsFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final ChatClient chatClient;
    private final PromptTemplates promptTemplates;
    private final AiResponseSanitizer aiResponseSanitizer;
    private final AiFitnessPlanValidator aiFitnessPlanValidator;
    private final DashScopeChatOptionsFactory dashScopeChatOptionsFactory;

    @Override
    public String chat(String message) {
        log.info("AI chat request, messageLength={}", message.length());
        try {
            return chatClient.prompt().user(message).call().content();
        } catch (Exception e) {
            log.error("AI chat invocation failed", e);
            throw new AiIntegrationException("AI 服务调用失败", e);
        }
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        log.info("AI chat request with system/user prompt, systemLength={}, userLength={}",
                systemPrompt != null ? systemPrompt.length() : 0,
                userPrompt != null ? userPrompt.length() : 0);
        try {
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI chat invocation with system/user prompt failed", e);
            throw new AiIntegrationException("AI 服务调用失败", e);
        }
    }

    @Override
    public String chatWithPrompt(String prompt, Map<String, Object> variables) {
        log.info("AI prompt request, variableCount={}", variables != null ? variables.size() : 0);
        try {
            String renderedPrompt = new PromptTemplate(prompt).render(variables);
            return chat(renderedPrompt);
        } catch (Exception e) {
            log.error("AI prompt invocation failed", e);
            throw new AiIntegrationException("AI 服务调用失败", e);
        }
    }

    @Override
    public Flux<String> streamChat(String message) {
        log.info("AI stream chat request, messageLength={}", message.length());
        try {
            return chatClient.prompt()
                    .user(message)
                    .stream()
                    .content()
                    .doOnComplete(() -> log.info("AI stream chat completed"))
                    .doOnError(e -> log.error("AI stream chat failed", e));
        } catch (Exception e) {
            log.error("AI stream chat invocation failed", e);
            return Flux.error(new AiIntegrationException("AI 流式服务调用失败", e));
        }
    }

    @Override
    public Flux<String> streamChatWithPrompt(String prompt, Map<String, Object> variables) {
        log.info("AI stream prompt request, variableCount={}", variables != null ? variables.size() : 0);
        try {
            String renderedPrompt = new PromptTemplate(prompt).render(variables);
            return streamChat(renderedPrompt);
        } catch (Exception e) {
            log.error("AI stream prompt invocation failed", e);
            return Flux.error(new AiIntegrationException("AI 流式服务调用失败", e));
        }
    }

    @Override
    public String generateFitnessPlan(String goal, String bodyPart, String experience,
                                      Integer height, Integer weight, Integer age) {
        log.info("Generate legacy fitness plan, goal={}, experience={}", goal, experience);
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("goal", goal);
            variables.put("bodyPart", bodyPart);
            variables.put("experience", experience);
            variables.put("height", height);
            variables.put("weight", weight);
            variables.put("age", age);
            AiPromptSpec prompt = promptTemplates.createLegacyFitnessPlanPrompt(variables);
            return chat(prompt.system(), prompt.user());
        } catch (Exception e) {
            log.error("Generate legacy fitness plan failed", e);
            throw new AiIntegrationException("生成健身计划失败", e);
        }
    }

    @Override
    public FitnessPlanResponseDTO generateFitnessPlanFromProfile(Map<String, Object> profile) {
        log.info("Generate fitness plan from profile");
        try {
            AiPromptSpec prompt = promptTemplates.createFitnessPlanPrompt(profile);
            BeanOutputConverter<FitnessPlanResponseDTO> converter =
                    new BeanOutputConverter<>(FitnessPlanResponseDTO.class);
            FitnessPlanResponseDTO response = parseFitnessPlanWithStructuredOutput(prompt, converter);
            aiFitnessPlanValidator.validateFitnessPlan(response);
            return response;
        } catch (Exception e) {
            log.error("Generate fitness plan from profile failed", e);
            throw new AiIntegrationException("生成健身计划失败", e);
        }
    }

    @Override
    public String analyzeFitnessData(Map<String, Object> variables) {
        log.info("Analyze fitness data");
        try {
            AiPromptSpec prompt = promptTemplates.createFitnessAnalysisPrompt(variables);
            return chat(prompt.system(), prompt.user());
        } catch (Exception e) {
            log.error("Analyze fitness data failed", e);
            throw new AiIntegrationException("健身数据分析失败", e);
        }
    }

    @Override
    public String getNutritionAdvice(Map<String, Object> variables) {
        log.info("Generate nutrition advice");
        try {
            AiPromptSpec prompt = promptTemplates.createNutritionAdvicePrompt(variables);
            return chat(prompt.system(), prompt.user());
        } catch (Exception e) {
            log.error("Generate nutrition advice failed", e);
            throw new AiIntegrationException("生成营养建议失败", e);
        }
    }

    @Override
    public String getExerciseGuide(Map<String, Object> variables) {
        log.info("Generate exercise guide");
        try {
            AiPromptSpec prompt = promptTemplates.createExerciseGuidePrompt(variables);
            return chat(prompt.system(), prompt.user());
        } catch (Exception e) {
            log.error("Generate exercise guide failed", e);
            throw new AiIntegrationException("生成运动动作指导失败", e);
        }
    }

    @Override
    public String polishText(String text) {
        log.info("Polish text, textLength={}", text.length());
        try {
            AiPromptSpec prompt = promptTemplates.createTextPolishPrompt(text);
            String response = chat(prompt.system(), prompt.user());
            return aiResponseSanitizer.cleanPolishedResponse(response);
        } catch (Exception e) {
            log.error("Polish text failed", e);
            throw new AiIntegrationException("文本润色失败", e);
        }
    }

    private FitnessPlanResponseDTO parseFitnessPlanWithStructuredOutput(
            AiPromptSpec prompt,
            BeanOutputConverter<FitnessPlanResponseDTO> converter) {
        try {
            return chatClient.prompt()
                    .system(prompt.system())
                    .user(prompt.user())
                    .options(dashScopeChatOptionsFactory.structuredJsonOutputOptions())
                    .call()
                    .entity(converter);
        } catch (Exception e) {
            log.warn("Structured output parsing failed, fallback to manual JSON conversion: {}", e.getMessage());
            return parseFitnessPlanWithManualFallback(prompt, converter);
        }
    }

    private FitnessPlanResponseDTO parseFitnessPlanWithManualFallback(
            AiPromptSpec prompt,
            BeanOutputConverter<FitnessPlanResponseDTO> converter) {
        String jsonResponse = chat(prompt.system(), prompt.user() + "\n\n" + converter.getFormat());
        String cleanedJson = aiResponseSanitizer.cleanJsonResponse(jsonResponse);
        return converter.convert(cleanedJson);
    }
}
