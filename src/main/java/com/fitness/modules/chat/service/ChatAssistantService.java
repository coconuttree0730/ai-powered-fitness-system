package com.fitness.modules.chat.service;

import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.vo.ChatMessageVO;
import com.fitness.modules.chat.model.vo.ChatSessionVO;
import com.fitness.modules.chat.model.vo.FitnessPlanCardVO;
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
}
