package com.fitness.modules.chat.service;

import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.vo.ChatMessageVO;
import com.fitness.modules.chat.model.vo.ChatSessionVO;
import com.fitness.modules.chat.model.vo.ChatStreamEventVO;
import com.fitness.modules.chat.model.vo.FitnessPlanCardVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatAssistantService {

    /**
     * 创建会话
     *
     * @return 会话ID
     */
    ChatSessionVO createSession(Long userId);
    /**
     * 发送消息
     *
     * @param dto 消息DTO
     * @return 消息VO
     */
    ChatMessageVO sendMessage(Long userId, ChatMessageDTO dto);
    /**
     * 发送消息（流式）
     *
     * @param dto 消息DTO
     * @return 响应流
     */
    Flux<ChatStreamEventVO> sendMessageStream(Long userId, ChatMessageDTO dto);
    /**
     * 获取用户会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSessionVO> getUserSessions(Long userId);

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 会话详情
     */
    ChatSessionVO getSessionDetail(Long sessionId, Long userId);

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     */
    void deleteSession(Long sessionId, Long userId);

    /**
     * 获取会话消息列表
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param lastMessageId 最后一条消息ID
     * @param limit     查询数量限制
     * @return 消息列表
     */
    List<ChatMessageVO> getSessionMessages(Long sessionId, Long userId, Long lastMessageId, Integer limit);

    /**
     * 生成健身计划卡片
     *
     * @param userId 用户ID
     * @param goal       健身目标
     * @param bodyPart   训练部位偏好
     * @param experience 健身经验
     * @return 健身计划卡片VO
     */
    FitnessPlanCardVO generateFitnessPlanCard(Long userId, String goal, String bodyPart, String experience);

    /**
     * 保存健身计划
     *
     * @param userId 用户ID
     * @param planCard 健身计划卡片
     * @return 保存的计划ID
     */
    Long saveFitnessPlan(Long userId, FitnessPlanCardVO planCard);

    /**
     * 获取用户的健身计划列表
     *
     * @param userId 用户ID
     * @return 健身计划卡片列表
     */
    List<FitnessPlanCardVO> getUserPlans(Long userId);

    /**
     * HITL：恢复被中断的 Agent 执行
     */
    Flux<ChatStreamEventVO> resumeWithApproval(Long userId, Long sessionId, String threadId, boolean approved);
}
