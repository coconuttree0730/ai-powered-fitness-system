package com.fitness.modules.chat.agent;

import com.fitness.modules.chat.model.vo.ChatStreamEventVO;
import reactor.core.publisher.Flux;

public interface JianXiaoZhuAgentService {

    String chat(Long userId, Long sessionId, String userMessage);

    Flux<ChatStreamEventVO> streamChat(Long userId, Long sessionId, String userMessage);
}
