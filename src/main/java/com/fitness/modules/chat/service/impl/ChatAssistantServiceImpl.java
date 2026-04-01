package com.fitness.modules.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.chat.mapper.ChatMessageMapper;
import com.fitness.modules.chat.mapper.ChatSessionMapper;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.entity.ChatMessage;
import com.fitness.modules.chat.model.entity.ChatSession;
import com.fitness.modules.chat.model.vo.ChatMessageVO;
import com.fitness.modules.chat.model.vo.ChatSessionVO;
import com.fitness.modules.chat.prompt.ChatPromptTemplates;
import com.fitness.modules.chat.service.ChatAssistantService;
import com.fitness.modules.chat.service.ChatContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAssistantServiceImpl implements ChatAssistantService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatContextService chatContextService;
    private final AIService aiService;
    private final ChatPromptTemplates chatPromptTemplates;

    private static final int MAX_CONTEXT_MESSAGES = 10;

    @Override
    @Transactional
    public ChatSessionVO createSession(Long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setIsDeleted(false);
        
        chatSessionMapper.insert(session);
        
        log.info("创建新会话: sessionId={}, userId={}", session.getId(), userId);
        
        return convertToSessionVO(session);
    }

    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long userId, ChatMessageDTO dto) {
        ChatSession session;
        if (dto.getSessionId() == null) {
            session = createSessionEntity(userId);
        } else {
            session = chatSessionMapper.selectById(dto.getSessionId());
            if (session == null || !session.getUserId().equals(userId)) {
                throw new BusinessException("会话不存在或无权访问");
            }
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        userMessage.setCreatedAt(LocalDateTime.now());
        
        chatContextService.saveMessageToDatabase(userMessage);
        chatContextService.addMessage(session.getId(), userMessage);

        String aiResponse = callAI(session.getId(), dto.getContent());

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(aiResponse);
        assistantMessage.setCreatedAt(LocalDateTime.now());
        
        chatContextService.saveMessageToDatabase(assistantMessage);
        chatContextService.addMessage(session.getId(), assistantMessage);

        if ("新对话".equals(session.getTitle())) {
            session.setTitle(generateSessionTitle(dto.getContent()));
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionMapper.updateById(session);
        }

        log.info("发送消息: sessionId={}, userId={}, 消息长度={}", session.getId(), userId, dto.getContent().length());

        return convertToMessageVO(assistantMessage);
    }

    @Override
    public Flux<String> sendMessageStream(Long userId, ChatMessageDTO dto) {
        ChatSession session;
        if (dto.getSessionId() == null) {
            session = createSessionEntity(userId);
        } else {
            session = chatSessionMapper.selectById(dto.getSessionId());
            if (session == null || !session.getUserId().equals(userId)) {
                return Flux.error(new BusinessException("会话不存在或无权访问"));
            }
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        userMessage.setCreatedAt(LocalDateTime.now());

        chatContextService.saveMessageToDatabase(userMessage);
        chatContextService.addMessage(session.getId(), userMessage);

        String fullPrompt = chatPromptTemplates.getSystemPrompt() + "\n\n用户问题：" + dto.getContent();

        StringBuilder fullResponse = new StringBuilder();

        // 保存当前安全上下文，用于在异步线程中恢复
        SecurityContext securityContext = SecurityContextHolder.getContext();

        return aiService.streamChat(fullPrompt)
            .doOnNext(fullResponse::append)
            .doOnComplete(() -> {
                // 在异步线程中恢复安全上下文
                SecurityContextHolder.setContext(securityContext);
                try {
                    ChatMessage assistantMessage = new ChatMessage();
                    assistantMessage.setSessionId(session.getId());
                    assistantMessage.setRole("assistant");
                    assistantMessage.setContent(fullResponse.toString());
                    assistantMessage.setCreatedAt(LocalDateTime.now());

                    chatContextService.saveMessageToDatabase(assistantMessage);
                    chatContextService.addMessage(session.getId(), assistantMessage);

                    if ("新对话".equals(session.getTitle())) {
                        session.setTitle(generateSessionTitle(dto.getContent()));
                        session.setUpdatedAt(LocalDateTime.now());
                        chatSessionMapper.updateById(session);
                    }

                    log.info("流式发送消息完成: sessionId={}, userId={}", session.getId(), userId);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public List<ChatSessionVO> getUserSessions(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectList(
            new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getIsDeleted, false)
                .orderByDesc(ChatSession::getUpdatedAt)
        );
        
        return sessions.stream()
            .map(this::convertToSessionVO)
            .collect(Collectors.toList());
    }

    @Override
    public ChatSessionVO getSessionDetail(Long sessionId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在或无权访问");
        }
        
        ChatSessionVO vo = convertToSessionVO(session);
        
        return vo;
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在或无权访问");
        }
        
        session.setIsDeleted(true);
        chatSessionMapper.updateById(session);
        
        chatContextService.clearContext(sessionId);
        
        log.info("删除会话: sessionId={}, userId={}", sessionId, userId);
    }

    @Override
    public List<ChatMessageVO> getSessionMessages(Long sessionId, Long userId, Long lastMessageId, Integer limit) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在或无权访问");
        }
        
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }
        
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
            .eq(ChatMessage::getSessionId, sessionId)
            .orderByDesc(ChatMessage::getCreatedAt)
            .last("LIMIT " + limit);
        
        if (lastMessageId != null) {
            ChatMessage lastMessage = chatMessageMapper.selectById(lastMessageId);
            if (lastMessage != null) {
                wrapper.lt(ChatMessage::getCreatedAt, lastMessage.getCreatedAt());
            }
        }
        
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        
        return messages.stream()
            .map(this::convertToMessageVO)
            .collect(Collectors.toList());
    }

    private ChatSession createSessionEntity(Long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setIsDeleted(false);
        chatSessionMapper.insert(session);
        return session;
    }

    private String callAI(Long sessionId, String userMessage) {
        List<ChatMessage> contextMessages = chatContextService.getContext(sessionId, MAX_CONTEXT_MESSAGES);
        
        List<Message> messages = buildMessages(contextMessages, userMessage);
        
        Prompt prompt = new Prompt(messages);
        
        return aiService.chat(userMessage);
    }

    private List<Message> buildMessages(List<ChatMessage> contextMessages, String currentMessage) {
        List<Message> messages = new ArrayList<>();
        
        messages.add(new SystemMessage(chatPromptTemplates.getSystemPrompt()));
        
        if (CollUtil.isNotEmpty(contextMessages)) {
            for (ChatMessage msg : contextMessages) {
                if ("user".equals(msg.getRole())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if ("assistant".equals(msg.getRole())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }
        
        messages.add(new UserMessage(currentMessage));
        
        return messages;
    }

    private String generateSessionTitle(String firstMessage) {
        if (StrUtil.isBlank(firstMessage)) {
            return "新对话";
        }
        
        int maxLength = 20;
        if (firstMessage.length() <= maxLength) {
            return firstMessage;
        }
        return firstMessage.substring(0, maxLength) + "...";
    }

    private ChatSessionVO convertToSessionVO(ChatSession session) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setCreatedAt(session.getCreatedAt());
        vo.setUpdatedAt(session.getUpdatedAt());
        return vo;
    }

    private ChatMessageVO convertToMessageVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }
}
