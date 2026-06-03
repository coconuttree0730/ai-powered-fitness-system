package com.fitness.modules.chat.agent;

import com.fitness.modules.chat.model.vo.ChatStreamEventVO;
import reactor.core.publisher.Flux;

public interface JianXiaoZhuAgentService {

    String chat(Long userId, Long sessionId, String userMessage);

    Flux<ChatStreamEventVO> streamChat(Long userId, Long sessionId, String userMessage);

    /**
     * HITL：恢复被中断的 Agent 执行
     * @param sessionId 会话ID（用于定位 checkpoint）
     * @param threadId 线程ID（与 sessionId 对应）
     * @param approved 是否批准执行
     */
    Flux<ChatStreamEventVO> resumeWithApproval(Long sessionId, String threadId, boolean approved);
}
