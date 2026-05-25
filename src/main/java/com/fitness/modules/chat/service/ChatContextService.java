package com.fitness.modules.chat.service;

import com.fitness.modules.chat.model.entity.ChatMessage;

import java.util.List;

public interface ChatContextService {

    void addMessage(Long sessionId, ChatMessage message);

    List<ChatMessage> getContext(Long sessionId, int maxMessages);

    void clearContext(Long sessionId);

    void saveMessageToDatabase(ChatMessage message);
}
