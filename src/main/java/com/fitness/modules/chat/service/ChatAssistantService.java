package com.fitness.modules.chat.service;

import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.vo.ChatMessageVO;
import com.fitness.modules.chat.model.vo.ChatSessionVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatAssistantService {

    ChatSessionVO createSession(Long userId);

    ChatMessageVO sendMessage(Long userId, ChatMessageDTO dto);

    Flux<String> sendMessageStream(Long userId, ChatMessageDTO dto);

    List<ChatSessionVO> getUserSessions(Long userId);

    ChatSessionVO getSessionDetail(Long sessionId, Long userId);

    void deleteSession(Long sessionId, Long userId);

    List<ChatMessageVO> getSessionMessages(Long sessionId, Long userId, Long lastMessageId, Integer limit);
}
