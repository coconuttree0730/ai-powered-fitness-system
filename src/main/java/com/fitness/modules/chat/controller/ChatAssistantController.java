package com.fitness.modules.chat.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.vo.ChatMessageVO;
import com.fitness.modules.chat.model.vo.ChatSessionVO;
import com.fitness.modules.chat.model.vo.FitnessPlanCardVO;
import com.fitness.modules.chat.service.ChatAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatAssistantController {

    private final ChatAssistantService chatAssistantService;

    @PostMapping("/sessions")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<ChatSessionVO> createSession() {
        Long userId = SecurityUtils.getCurrentUserId();
        ChatSessionVO session = chatAssistantService.createSession(userId);
        return Result.success(session);
    }

    @PostMapping("/messages")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<ChatMessageVO> sendMessage(@Valid @RequestBody ChatMessageDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ChatMessageVO message = chatAssistantService.sendMessage(userId, dto);
        return Result.success(message);
    }

    @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageStream(@Valid @RequestBody ChatMessageDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Flux.error(new org.springframework.security.access.AccessDeniedException("未登录或登录已过期"));
        }
        //编程式鉴权
        if (!SecurityUtils.hasRole("MEMBER")) {
            return Flux.error(new org.springframework.security.access.AccessDeniedException("没有权限访问该资源"));
        }
        return chatAssistantService.sendMessageStream(userId, dto);
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<List<ChatSessionVO>> getUserSessions() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ChatSessionVO> sessions = chatAssistantService.getUserSessions(userId);
        return Result.success(sessions);
    }

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话详情
     */
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<ChatSessionVO> getSessionDetail(@PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ChatSessionVO session = chatAssistantService.getSessionDetail(sessionId, userId);
        return Result.success(session);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        chatAssistantService.deleteSession(sessionId, userId);
        return Result.success(null);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<List<ChatMessageVO>> getSessionMessages(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ChatMessageVO> messages = chatAssistantService.getSessionMessages(sessionId, userId, lastMessageId, limit);
        return Result.success(messages);
    }

    /**
     * 生成健身计划卡片
     *
     * @param goal       健身目标
     * @param bodyPart   训练部位
     * @param experience 经验水平
     * @return 健身计划卡片
     */
    @PostMapping("/fitness-plan/generate")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<FitnessPlanCardVO> generateFitnessPlan(
            @RequestParam String goal,
            @RequestParam String bodyPart,
            @RequestParam String experience) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("生成健身计划卡片请求: userId={}, goal={}, bodyPart={}, experience={}",
                userId, goal, bodyPart, experience);
        FitnessPlanCardVO planCard = chatAssistantService.generateFitnessPlanCard(userId, goal, bodyPart, experience);
        return Result.success(planCard);
    }

    /**
     * 保存健身计划
     *
     * @param planCard 健身计划卡片
     * @return 保存的计划ID
     */
    @PostMapping("/fitness-plan/save")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Long> saveFitnessPlan(@RequestBody FitnessPlanCardVO planCard) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("保存健身计划请求: userId={}, planName={}", userId, planCard.getPlanName());
        Long planId = chatAssistantService.saveFitnessPlan(userId, planCard);
        return Result.success(planId);
    }

    /**
     * 获取我的健身计划列表
     *
     * @return 健身计划卡片列表
     */
    @GetMapping("/fitness-plan/my")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<List<FitnessPlanCardVO>> getMyFitnessPlans() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取我的健身计划列表: userId={}", userId);
        List<FitnessPlanCardVO> plans = chatAssistantService.getUserPlans(userId);
        return Result.success(plans);
    }
}
