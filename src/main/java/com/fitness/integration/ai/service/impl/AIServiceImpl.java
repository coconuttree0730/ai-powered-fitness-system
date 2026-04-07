package com.fitness.integration.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import com.fitness.integration.ai.prompt.PromptTemplates;
import com.fitness.integration.ai.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
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
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.dashscope.chat.options.model:qwen-plus}")
    private String model;

    @Override
    public String chat(String message) {
        log.info("AI 通用对话请求，消息长度: {}", message.length());
        
        try {
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            log.info("AI 对话响应成功，响应长度: {}", response != null ? response.length() : 0);
            return response;
        } catch (Exception e) {
            log.error("AI 对话调用失败", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String chatWithPrompt(String prompt, Map<String, Object> variables) {
        log.info("AI Prompt 对话请求，变量数: {}", variables != null ? variables.size() : 0);

        try {
            PromptTemplate promptTemplate = new PromptTemplate(prompt);
            String renderedPrompt = promptTemplate.render(variables);
            log.debug("渲染后的 Prompt 长度: {}", renderedPrompt.length());

            String response = chatClient.prompt()
                    .user(renderedPrompt)
                    .call()
                    .content();

            log.info("AI Prompt 对话响应成功，响应长度: {}", response != null ? response.length() : 0);
            return response;
        } catch (Exception e) {
            log.error("AI Prompt 对话调用失败", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> streamChat(String message) {
        log.info("AI 流式对话请求，消息长度: {}", message.length());

        try {
            return chatClient.prompt()
                    .user(message)
                    .stream()
                    .content()
                    .doOnComplete(() -> {
                        log.info("AI 流式对话完成");
                    })
                    .doOnError(e -> {
                        log.error("AI 流式对话失败", e);
                    });
        } catch (Exception e) {
            log.error("AI 流式对话调用失败", e);
            return Flux.error(new RuntimeException("AI 流式服务调用失败: " + e.getMessage(), e));
        }
    }

    @Override
    public Flux<String> streamChatWithPrompt(String prompt, Map<String, Object> variables) {
        log.info("AI Prompt 流式对话请求，变量数: {}", variables != null ? variables.size() : 0);

        try {
            PromptTemplate promptTemplate = new PromptTemplate(prompt);
            String renderedPrompt = promptTemplate.render(variables);

            return chatClient.prompt()
                    .user(renderedPrompt)
                    .stream()
                    .content()
                    .doOnComplete(() -> {
                        log.info("AI Prompt 流式对话完成");
                    })
                    .doOnError(e -> {
                        log.error("AI Prompt 流式对话失败", e);
                    });
        } catch (Exception e) {
            log.error("AI Prompt 流式对话调用失败", e);
            return Flux.error(new RuntimeException("AI 流式服务调用失败: " + e.getMessage(), e));
        }
    }

    @Override
    public String generateFitnessPlan(String goal, String bodyPart, String experience,
                                      Integer height, Integer weight, Integer age) {
        log.info("生成健身计划请求，目标: {}, 经验: {}", goal, experience);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("goal", goal);
            variables.put("bodyPart", bodyPart);
            variables.put("experience", experience);
            variables.put("height", height);
            variables.put("weight", weight);
            variables.put("age", age);

            String prompt = promptTemplates.generateFitnessPlan(variables);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("健身计划生成成功，响应长度: {}", response != null ? response.length() : 0);
            //log.info("健身计划-LLM 返回消息{}",response);
            return response;
        } catch (Exception e) {
            log.error("生成健身计划失败", e);
            throw new RuntimeException("生成健身计划失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FitnessPlanResponseDTO generateFitnessPlanFromProfile(Map<String, Object> profile) {
        log.info("从个人档案生成健身计划请求，用户信息: {}", profile);

        try {
            String prompt = promptTemplates.generateFitnessPlanJson(profile);
            log.debug("生成的Prompt长度: {}", prompt.length());

            BeanOutputConverter<FitnessPlanResponseDTO> converter = 
                    new BeanOutputConverter<>(FitnessPlanResponseDTO.class);
            
            String format = converter.getFormat();
            log.debug("生成的Output Schema长度: {}", format.length());

            String fullPrompt = prompt + "\n\n" + format;
            log.debug("完整Prompt长度: {}", fullPrompt.length());

            String jsonResponse = chatClient.prompt()
                    .user(fullPrompt)
                    .call()
                    .content();

            log.debug("AI返回的JSON长度: {}", jsonResponse != null ? jsonResponse.length() : 0);
            log.info("=== LLM返回的完整JSON内容 ===");
            log.info("{}", jsonResponse);
            log.info("=== JSON内容结束 ===");

            String cleanedJson = cleanJsonResponse(jsonResponse);
            log.debug("清洗后的JSON长度: {}", cleanedJson.length());

            FitnessPlanResponseDTO response = objectMapper.readValue(cleanedJson, FitnessPlanResponseDTO.class);

            log.info("从个人档案生成健身计划成功");
            log.debug("生成的计划: subtitle={}, weeklyPlan.size={}", 
                    response.getSubtitle(), 
                    response.getWeeklyPlan() != null ? response.getWeeklyPlan().size() : 0);
            
            return response;
        } catch (Exception e) {
            log.error("从个人档案生成健身计划失败", e);
            throw new RuntimeException("生成健身计划失败: " + e.getMessage(), e);
        }
    }

    private String cleanJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return response;
        }
        
        String cleaned = response.trim();
        
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        return cleaned.trim();
    }

    @Override
    public String analyzeFitnessData(Map<String, Object> variables) {
        log.info("健身数据分析请求");

        try {
            String prompt = promptTemplates.generateFitnessAnalysis(variables);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("健身数据分析完成，响应长度: {}", response != null ? response.length() : 0);
            return response;
        } catch (Exception e) {
            log.error("健身数据分析失败", e);
            throw new RuntimeException("健身数据分析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getNutritionAdvice(Map<String, Object> variables) {
        log.info("营养建议请求");

        try {
            String prompt = promptTemplates.generateNutritionAdvice(variables);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("营养建议生成成功，响应长度: {}", response != null ? response.length() : 0);
            return response;
        } catch (Exception e) {
            log.error("生成营养建议失败", e);
            throw new RuntimeException("生成营养建议失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getExerciseGuide(Map<String, Object> variables) {
        log.info("运动动作指导请求");

        try {
            String prompt = promptTemplates.generateExerciseGuide(variables);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("运动动作指导生成成功，响应长度: {}", response != null ? response.length() : 0);
            return response;
        } catch (Exception e) {
            log.error("生成运动动作指导失败", e);
            throw new RuntimeException("生成运动动作指导失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String polishText(String text) {
        log.info("文本润色请求，文本长度: {}", text.length());
        log.debug("待润色原文: {}", text);

        try {
            String systemPrompt = promptTemplates.getTextPolishSystemPrompt();
            String userPrompt = promptTemplates.generateTextPolishUserPrompt(text);

            log.debug("System Prompt: {}", systemPrompt);
            log.debug("User Prompt: {}", userPrompt);

            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("LLM返回原始响应，长度: {}", response != null ? response.length() : 0);
            log.debug("LLM返回的原始内容: {}", response);

            String cleaned = cleanPolishedResponse(response);

            log.info("文本润色成功，原始响应长度: {}，清洗后长度: {}",
                    response != null ? response.length() : 0,
                    cleaned != null ? cleaned.length() : 0);
            log.debug("清洗后的润色结果: {}", cleaned);
            return cleaned;
        } catch (Exception e) {
            log.error("文本润色失败，原文: {}", text, e);
            throw new RuntimeException("文本润色失败: " + e.getMessage(), e);
        }
    }

    private String cleanPolishedResponse(String response) {
        if (response == null || response.isBlank()) {
            return response;
        }

        String result = response;

        result = result.replaceAll("[（(][\\d约]+[字词][）)]", "");
        result = result.replaceAll("[【\\[]\\d+[字词][】\\]]", "");
        result = result.replaceAll("^[\\s]*$", "");

        result = result.trim();
        result = result.replaceAll("\\n{3,}", "\n\n");
        result = result.trim();

        return result;
    }
}
