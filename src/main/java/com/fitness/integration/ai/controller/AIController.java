package com.fitness.integration.ai.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.ai.model.dto.TextPolishDTO;
import com.fitness.integration.ai.model.vo.TextPolishVO;
import com.fitness.integration.ai.service.AIService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI 控制器
 * 提供 AI 对话接口（测试用，需要管理员权限）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    /**
     * 通用对话接口
     *
     * @param request 对话请求
     * @return AI 回复
     */
    @PostMapping("/chat")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> chat(@Valid @RequestBody ChatRequest request) {
        log.info("AI 对话请求: {}", request.getMessage());
        String response = aiService.chat(request.getMessage());
        return Result.success(response);
    }

    /**
     * 使用 PromptTemplate 的对话接口
     *
     * @param request Prompt 对话请求
     * @return AI 回复
     */
    @PostMapping("/chat/prompt")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> chatWithPrompt(@Valid @RequestBody PromptChatRequest request) {
        log.info("AI Prompt 对话请求，模板长度: {}", request.getPrompt().length());
        String response = aiService.chatWithPrompt(request.getPrompt(), request.getVariables());
        return Result.success(response);
    }

    /**
     * 流式对话接口
     *
     * @param request 对话请求
     * @return AI 流式回复
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) {
        log.info("AI 流式对话请求: {}", request.getMessage());
        return aiService.streamChat(request.getMessage());
    }

    /**
     * 生成健身计划
     *
     * @param request 健身计划请求
     * @return 生成的健身计划
     */
    @PostMapping("/fitness-plan")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('MEMBER')")
    public Result<String> generateFitnessPlan(@Valid @RequestBody FitnessPlanRequest request) {
        log.info("生成健身计划请求，用户: {}, 目标: {}", request.getUserId(), request.getGoal());
        String plan = aiService.generateFitnessPlan(
                request.getGoal(),
                request.getBodyPart(),
                request.getExperience(),
                request.getHeight(),
                request.getWeight(),
                request.getAge()
        );
        return Result.success(plan);
    }

    /**
     * 分析健身数据
     *
     * @param request 数据分析请求
     * @return 数据分析报告
     */
    @PostMapping("/analyze")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('MEMBER')")
    public Result<String> analyzeFitnessData(@Valid @RequestBody FitnessAnalysisRequest request) {
        log.info("健身数据分析请求，用户: {}", request.getUserId());
        String analysis = aiService.analyzeFitnessData(request.getVariables());
        return Result.success(analysis);
    }

    /**
     * 获取营养建议
     *
     * @param request 营养建议请求
     * @return 营养建议
     */
    @PostMapping("/nutrition")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('MEMBER')")
    public Result<String> getNutritionAdvice(@Valid @RequestBody NutritionAdviceRequest request) {
        log.info("营养建议请求，用户: {}", request.getUserId());
        String advice = aiService.getNutritionAdvice(request.getVariables());
        return Result.success(advice);
    }

    /**
     * 获取运动动作指导
     *
     * @param request 动作指导请求
     * @return 动作指导
     */
    @PostMapping("/exercise-guide")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('MEMBER')")
    public Result<String> getExerciseGuide(@Valid @RequestBody ExerciseGuideRequest request) {
        log.info("运动动作指导请求，动作: {}", request.getExerciseName());
        String guide = aiService.getExerciseGuide(request.getVariables());
        return Result.success(guide);
    }

    /**
     * 文本润色接口
     *
     * @param request 润色请求
     * @return 润色结果
     */
    @PostMapping("/polish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<TextPolishVO> polishText(@Valid @RequestBody TextPolishDTO request) {
        log.info("文本润色请求，文本长度: {}", request.getText().length());
        String polishedText = aiService.polishText(request.getText());
        
        TextPolishVO vo = new TextPolishVO();
        vo.setPolishedText(polishedText);
        vo.setOriginalText(request.getText());
        
        return Result.success(vo);
    }

    // ==================== 请求 DTO ====================

    @Data
    public static class ChatRequest {
        @NotBlank(message = "消息内容不能为空")
        private String message;
    }

    @Data
    public static class PromptChatRequest {
        @NotBlank(message = "Prompt 模板不能为空")
        private String prompt;
        private Map<String, Object> variables;
    }

    @Data
    public static class FitnessPlanRequest {
        private Long userId;
        @NotBlank(message = "健身目标不能为空")
        private String goal;
        private String bodyPart;
        @NotBlank(message = "健身经验不能为空")
        private String experience;
        private Integer height;
        private Integer weight;
        private Integer age;
    }

    @Data
    public static class FitnessAnalysisRequest {
        private Long userId;
        private Map<String, Object> variables;
    }

    @Data
    public static class NutritionAdviceRequest {
        private Long userId;
        private Map<String, Object> variables;
    }

    @Data
    public static class ExerciseGuideRequest {
        @NotBlank(message = "动作名称不能为空")
        private String exerciseName;
        private Map<String, Object> variables;
    }
}
