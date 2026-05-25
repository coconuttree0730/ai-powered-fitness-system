package com.fitness.integration.ai.service;

import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI 服务接口
 * 提供与阿里百炼大模型的交互能力
 */
public interface AIService {

    /**
     * 通用对话接口
     *
     * @param message 用户输入消息
     * @return AI 回复内容
     */
    String chat(String message);

    /**
     * 使用 system / user 双层提示词对话。
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return AI 回复内容
     */
    String chat(String systemPrompt, String userPrompt);

    /**
     * 使用 PromptTemplate 的对话接口
     *
     * @param prompt    Prompt 模板字符串
     * @param variables 模板变量
     * @return AI 回复内容
     */
    String chatWithPrompt(String prompt, Map<String, Object> variables);

    /**
     * 流式对话接口
     *
     * @param message 用户输入消息
     * @return AI 回复流
     */
    Flux<String> streamChat(String message);

    /**
     * 使用 PromptTemplate 的流式对话接口
     *
     * @param prompt    Prompt 模板字符串
     * @param variables 模板变量
     * @return AI 回复流
     */
    Flux<String> streamChatWithPrompt(String prompt, Map<String, Object> variables);

    /**
     * 生成健身计划
     *
     * @param goal       健身目标
     * @param bodyPart   训练部位偏好
     * @param experience 健身经验
     * @param height     身高(cm)
     * @param weight     体重(kg)
     * @param age        年龄
     * @return 健身计划内容
     */
    String generateFitnessPlan(String goal, String bodyPart, String experience,
                               Integer height, Integer weight, Integer age);

    /**
     * 从个人档案生成健身计划（返回结构化JSON）
     * 使用Spring AI的结构化输出功能，强制约束AI返回符合格式的JSON
     *
     * @param profile 个人档案数据Map
     * @return 结构化的健身计划DTO对象
     */
    FitnessPlanResponseDTO generateFitnessPlanFromProfile(Map<String, Object> profile);

    /**
     * 分析健身数据
     *
     * @param variables 分析所需的变量
     * @return 数据分析报告
     */
    String analyzeFitnessData(Map<String, Object> variables);

    /**
     * 获取营养建议
     *
     * @param variables 营养建议所需的变量
     * @return 营养建议内容
     */
    String getNutritionAdvice(Map<String, Object> variables);

    /**
     * 获取运动动作指导
     *
     * @param variables 动作指导所需的变量
     * @return 动作指导内容
     */
    String getExerciseGuide(Map<String, Object> variables);

    /**
     * 文本润色
     *
     * @param text 原始文本
     * @return 润色后的文本
     */
    String polishText(String text);
}
