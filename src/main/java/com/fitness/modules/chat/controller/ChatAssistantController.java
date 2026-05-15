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
import org.springframework.http.codec.ServerSentEvent;
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
    private final ObjectMapper objectMapper;

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
    public Flux<ServerSentEvent<String>> sendMessageStream(@Valid @RequestBody ChatMessageDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Flux.error(new org.springframework.security.access.AccessDeniedException("User is not authenticated"));
        }
        if (!SecurityUtils.hasRole("MEMBER")) {
            return Flux.error(new org.springframework.security.access.AccessDeniedException("Access denied"));
        }

        return chatAssistantService.sendMessageStream(userId, dto)
                .map(event -> ServerSentEvent.<String>builder()
                        .id(event.getType())
                        .event(event.getType())
                        .data(toJson(event))
                        .build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data(toJson(ChatStreamEventVO.done()))
                        .build()))
                .onErrorResume(ex -> {
                    log.error("Failed to process chat stream", ex);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data(toJson(ChatStreamEventVO.error("抱歉，健小助暂时无法完成本次回答，请稍后重试。")))
                            .build());
                });
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

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload to JSON", e);
            return "{}";
        }
    }
}
