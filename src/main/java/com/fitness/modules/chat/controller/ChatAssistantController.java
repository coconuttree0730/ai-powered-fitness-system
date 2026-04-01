package com.fitness.modules.chat.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.vo.ChatMessageVO;
import com.fitness.modules.chat.model.vo.ChatSessionVO;
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
    @PreAuthorize("hasRole('MEMBER')")
    public Flux<String> sendMessageStream(@Valid @RequestBody ChatMessageDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return chatAssistantService.sendMessageStream(userId, dto);
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
}
