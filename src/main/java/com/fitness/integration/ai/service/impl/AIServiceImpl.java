package com.fitness.integration.ai.service.impl;

import com.fitness.integration.ai.prompt.PromptTemplates;
import com.fitness.integration.ai.service.AIService;
import com.fitness.integration.ai.service.LangSmithService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 服务实现类
 * 使用 Spring AI Alibaba 与百炼大模型交互
 * 集成 LangSmith 追踪功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final ChatClient chatClient;
    private final PromptTemplates promptTemplates;
    private final LangSmithService langSmithService;

    @Value("${spring.ai.dashscope.chat.options.model:qwen-plus}")
    private String model;

    @Override
    public String chat(String message) {
        log.info("AI 通用对话请求，消息长度: {}", message.length());
        String traceId = langSmithService.startTrace("chat", Map.of("type", "general"));
        long startTime = System.currentTimeMillis();

        try {
            langSmithService.logInput(traceId, message, model);

            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            langSmithService.logOutput(traceId, response, duration);
            log.info("AI 对话响应成功，响应长度: {}, 耗时: {}ms", response != null ? response.length() : 0, duration);
            return response;
        } catch (Exception e) {
            langSmithService.logError(traceId, e.getMessage());
            log.error("AI 对话调用失败", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        } finally {
            langSmithService.endTrace(traceId);
        }
    }

    @Override
    public String chatWithPrompt(String prompt, Map<String, Object> variables) {
        log.info("AI Prompt 对话请求，变量数: {}", variables != null ? variables.size() : 0);
        String traceId = langSmithService.startTrace("chatWithPrompt", Map.of("type", "prompt_template"));
        long startTime = System.currentTimeMillis();

        try {
            PromptTemplate promptTemplate = new PromptTemplate(prompt);
            String renderedPrompt = promptTemplate.render(variables);
            log.debug("渲染后的 Prompt 长度: {}", renderedPrompt.length());

            langSmithService.logInput(traceId, renderedPrompt, model);

            String response = chatClient.prompt()
                    .user(renderedPrompt)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            langSmithService.logOutput(traceId, response, duration);
            log.info("AI Prompt 对话响应成功，响应长度: {}, 耗时: {}ms", response != null ? response.length() : 0, duration);
            return response;
        } catch (Exception e) {
            langSmithService.logError(traceId, e.getMessage());
            log.error("AI Prompt 对话调用失败", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        } finally {
            langSmithService.endTrace(traceId);
        }
    }

    @Override
    public Flux<String> streamChat(String message) {
        log.info("AI 流式对话请求，消息长度: {}", message.length());
        String traceId = langSmithService.startTrace("streamChat", Map.of("type", "stream"));
        long startTime = System.currentTimeMillis();

        try {
            langSmithService.logInput(traceId, message, model);

            return chatClient.prompt()
                    .user(message)
                    .stream()
                    .content()
                    //.doOnNext(chunk -> log.debug("收到流式响应块"))
                    .doOnComplete(() -> {
                        long duration = System.currentTimeMillis() - startTime;
                        langSmithService.logOutput(traceId, "Stream completed", duration);
                        langSmithService.endTrace(traceId);
                        log.info("AI 流式对话完成，耗时: {}ms", duration);
                    })
                    .doOnError(e -> {
                        langSmithService.logError(traceId, e.getMessage());
                        langSmithService.endTrace(traceId);
                        log.error("AI 流式对话失败", e);
                    });
        } catch (Exception e) {
            langSmithService.logError(traceId, e.getMessage());
            langSmithService.endTrace(traceId);
            log.error("AI 流式对话调用失败", e);
            return Flux.error(new RuntimeException("AI 流式服务调用失败: " + e.getMessage(), e));
        }
    }

    @Override
    public Flux<String> streamChatWithPrompt(String prompt, Map<String, Object> variables) {
        log.info("AI Prompt 流式对话请求，变量数: {}", variables != null ? variables.size() : 0);
        String traceId = langSmithService.startTrace("streamChatWithPrompt", Map.of("type", "stream_prompt"));
        long startTime = System.currentTimeMillis();

        try {
            PromptTemplate promptTemplate = new PromptTemplate(prompt);
            String renderedPrompt = promptTemplate.render(variables);

            langSmithService.logInput(traceId, renderedPrompt, model);

            return chatClient.prompt()
                    .user(renderedPrompt)
                    .stream()
                    .content()
                    //.doOnNext(chunk -> log.debug("收到流式响应块"))
                    .doOnComplete(() -> {
                        long duration = System.currentTimeMillis() - startTime;
                        langSmithService.logOutput(traceId, "Stream completed", duration);
                        langSmithService.endTrace(traceId);
                        log.info("AI Prompt 流式对话完成，耗时: {}ms", duration);
                    })
                    .doOnError(e -> {
                        langSmithService.logError(traceId, e.getMessage());
                        langSmithService.endTrace(traceId);
                        log.error("AI Prompt 流式对话失败", e);
                    });
        } catch (Exception e) {
            langSmithService.logError(traceId, e.getMessage());
            langSmithService.endTrace(traceId);
            log.error("AI Prompt 流式对话调用失败", e);
            return Flux.error(new RuntimeException("AI 流式服务调用失败: " + e.getMessage(), e));
        }
    }

    @Override
    public String generateFitnessPlan(String goal, String bodyPart, String experience,
                                      Integer height, Integer weight, Integer age) {
        log.info("生成健身计划请求，目标: {}, 经验: {}", goal, experience);
        String traceId = langSmithService.startTrace("generateFitnessPlan",
                Map.of("goal", goal, "experience", experience));
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("goal", goal);
            variables.put("bodyPart", bodyPart);
            variables.put("experience", experience);
            variables.put("height", height);
            variables.put("weight", weight);
            variables.put("age", age);

            String prompt = promptTemplates.generateFitnessPlan(variables);
            langSmithService.logInput(traceId, prompt, model);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            langSmithService.logOutput(traceId, response, duration);
            log.info("健身计划生成成功，响应长度: {}, 耗时: {}ms", response != null ? response.length() : 0, duration);
            return response;
        } catch (Exception e) {
            langSmithService.logError(traceId, e.getMessage());
            log.error("生成健身计划失败", e);
            throw new RuntimeException("生成健身计划失败: " + e.getMessage(), e);
        } finally {
            langSmithService.endTrace(traceId);
        }
    }

    @Override
    public String analyzeFitnessData(Map<String, Object> variables) {
        log.info("健身数据分析请求");
        String traceId = langSmithService.startTrace("analyzeFitnessData", Map.of("type", "analysis"));
        long startTime = System.currentTimeMillis();

        try {
            String prompt = promptTemplates.generateFitnessAnalysis(variables);
            langSmithService.logInput(traceId, prompt, model);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            langSmithService.logOutput(traceId, response, duration);
            log.info("健身数据分析完成，响应长度: {}, 耗时: {}ms", response != null ? response.length() : 0, duration);
            return response;
        } catch (Exception e) {
            langSmithService.logError(traceId, e.getMessage());
            log.error("健身数据分析失败", e);
            throw new RuntimeException("健身数据分析失败: " + e.getMessage(), e);
        } finally {
            langSmithService.endTrace(traceId);
        }
    }

    @Override
    public String getNutritionAdvice(Map<String, Object> variables) {
        log.info("营养建议请求");
        String traceId = langSmithService.startTrace("getNutritionAdvice", Map.of("type", "nutrition"));
        long startTime = System.currentTimeMillis();

        try {
            String prompt = promptTemplates.generateNutritionAdvice(variables);
            langSmithService.logInput(traceId, prompt, model);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            langSmithService.logOutput(traceId, response, duration);
            log.info("营养建议生成成功，响应长度: {}, 耗时: {}ms", response != null ? response.length() : 0, duration);
            return response;
        } catch (Exception e) {
            langSmithService.logError(traceId, e.getMessage());
            log.error("生成营养建议失败", e);
            throw new RuntimeException("生成营养建议失败: " + e.getMessage(), e);
        } finally {
            langSmithService.endTrace(traceId);
        }
    }

    @Override
    public String getExerciseGuide(Map<String, Object> variables) {
        log.info("运动动作指导请求");
        String traceId = langSmithService.startTrace("getExerciseGuide", Map.of("type", "exercise_guide"));
        long startTime = System.currentTimeMillis();

        try {
            String prompt = promptTemplates.generateExerciseGuide(variables);
            langSmithService.logInput(traceId, prompt, model);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            langSmithService.logOutput(traceId, response, duration);
            log.info("运动动作指导生成成功，响应长度: {}, 耗时: {}ms", response != null ? response.length() : 0, duration);
            return response;
        } catch (Exception e) {
            langSmithService.logError(traceId, e.getMessage());
            log.error("生成运动动作指导失败", e);
            throw new RuntimeException("生成运动动作指导失败: " + e.getMessage(), e);
        } finally {
            langSmithService.endTrace(traceId);
        }
    }
}
