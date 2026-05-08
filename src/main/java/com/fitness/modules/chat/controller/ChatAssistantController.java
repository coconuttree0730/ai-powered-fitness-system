package com.fitness.modules.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.vo.ChatMessageVO;
import com.fitness.modules.chat.model.vo.ChatSessionVO;
import com.fitness.modules.chat.model.vo.ChatStreamEventVO;
import com.fitness.modules.chat.model.vo.FitnessPlanCardVO;
import com.fitness.modules.chat.service.ChatAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatAssistantController {

    private final ChatAssistantService chatAssistantService;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

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
    public SseEmitter sendMessageStream(@Valid @RequestBody ChatMessageDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new org.springframework.security.access.AccessDeniedException("User is not authenticated");
        }
        if (!SecurityUtils.hasRole("MEMBER")) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        SseEmitter emitter = new SseEmitter(0L);
        executorService.execute(() -> {
            try {
                chatAssistantService.sendMessageStream(userId, dto)
                        .doOnNext(event -> sendEvent(emitter, event.getType(), event))
                        .doOnComplete(() -> {
                            sendEvent(emitter, "done", ChatStreamEventVO.done());
                            emitter.complete();
                        })
                        .blockLast();
            } catch (Exception ex) {
                log.error("Failed to process chat stream", ex);
                sendEvent(emitter, "error", ChatStreamEventVO.error("抱歉，健小助暂时无法完成本次回答，请稍后重试。"));
                emitter.complete();
            }
        });
        return emitter;
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<List<ChatSessionVO>> getUserSessions() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ChatSessionVO> sessions = chatAssistantService.getUserSessions(userId);
        return Result.success(sessions);
    }

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

    @PostMapping("/fitness-plan/generate")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<FitnessPlanCardVO> generateFitnessPlan(
            @RequestParam String goal,
            @RequestParam String bodyPart,
            @RequestParam String experience) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Generate fitness plan request: userId={}, goal={}, bodyPart={}, experience={}",
                userId, goal, bodyPart, experience);
        FitnessPlanCardVO planCard = chatAssistantService.generateFitnessPlanCard(userId, goal, bodyPart, experience);
        return Result.success(planCard);
    }

    @PostMapping("/fitness-plan/save")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Long> saveFitnessPlan(@RequestBody FitnessPlanCardVO planCard) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Save fitness plan request: userId={}, planName={}", userId, planCard.getPlanName());
        Long planId = chatAssistantService.saveFitnessPlan(userId, planCard);
        return Result.success(planId);
    }

    @GetMapping("/fitness-plan/my")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<List<FitnessPlanCardVO>> getMyFitnessPlans() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("List my fitness plans: userId={}", userId);
        List<FitnessPlanCardVO> plans = chatAssistantService.getUserPlans(userId);
        return Result.success(plans);
    }

    private void sendEvent(SseEmitter emitter, String eventName, ChatStreamEventVO payload) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(objectMapper.writeValueAsString(payload)));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize SSE payload", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to send SSE payload", ex);
        }
    }
}
