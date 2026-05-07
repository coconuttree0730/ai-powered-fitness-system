package com.fitness.modules.chat.agent;

import reactor.core.publisher.Flux;

public interface JianXiaoZhuAgentService {

    String chat(Long userId, Long sessionId, String userMessage);

    Flux<String> streamChat(Long userId, Long sessionId, String userMessage);
}
