package com.fitness.modules.chat.agent;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.chat.memory.service.LongTermMemoryService;
import com.fitness.modules.chat.prompt.ChatPromptTemplates;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;
import com.fitness.modules.user.service.UserFitnessProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class JianXiaoZhuAgentServiceImpl implements JianXiaoZhuAgentService {

    private final ReactAgent reactAgent;
    private final DatabaseStore databaseStore;
    private final LongTermMemoryService longTermMemoryService;
    private final UserFitnessProfileService userFitnessProfileService;
    private final ChatPromptTemplates chatPromptTemplates;

    @Override
    public String chat(Long userId, Long sessionId, String userMessage) {
        // 1. 加载上下文内容
        log.info("\n========== 加载上下文内容 ==========");
        
        UserFitnessProfileVO profile = userFitnessProfileService.getProfile(userId);
        longTermMemoryService.refreshProfileMemories(userId, profile);
        String memoryContext = longTermMemoryService.buildMemoryContext(userId);
        
        if (!memoryContext.isBlank()) {
            log.info("长期记忆上下文:\n{}", memoryContext);
        } else {
            log.info("长期记忆上下文: 无");
        }
        log.info("========== 上下文加载完成 ==========\n");

        // 2. 构建最终Prompt
        StringBuilder promptBuilder = new StringBuilder();
        if (!memoryContext.isBlank()) {
            promptBuilder.append("用户长期记忆:\n").append(memoryContext).append("\n\n");
        }
        promptBuilder.append("用户问题:\n").append(userMessage);
        String finalPrompt = promptBuilder.toString();

        // 3. 打印 systemmessage
        String systemPrompt = chatPromptTemplates.getSystemPrompt();
        log.info("\n========== systemmessage ==========\n{}\n========== systemmessage end ==========\n", systemPrompt);

        // 4. 打印 humanmessage
        log.info("\n========== humanmessage ==========\n{}\n========== humanmessage end ==========\n", finalPrompt);

        // 5. 调用ReactAgent
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(String.valueOf(sessionId))
                .store(databaseStore)
                .build();

        try {
            AssistantMessage message = reactAgent.call(finalPrompt, runnableConfig);
            String response = message.getText();

            // 6. 打印 aimessage（完整的LLM返回内容）
            log.info("\n========== aimessage (LLM完整返回) ==========\n{}\n========== aimessage end ==========\n", response);
            return response;
        } catch (GraphRunnerException ex) {
            log.error("Agent执行异常: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.AI_GENERATE_ERROR, "健小助暂时无法完成本次查询: " + ex.getMessage());
        }
    }

    @Override
    public Flux<String> streamChat(Long userId, Long sessionId, String userMessage) {
        // 1. 加载上下文内容
        log.info("\n========== 加载上下文内容 ==========");
        
        UserFitnessProfileVO profile = userFitnessProfileService.getProfile(userId);
        longTermMemoryService.refreshProfileMemories(userId, profile);
        String memoryContext = longTermMemoryService.buildMemoryContext(userId);
        
        if (!memoryContext.isBlank()) {
            log.info("长期记忆上下文:\n{}", memoryContext);
        } else {
            log.info("长期记忆上下文: 无");
        }
        log.info("========== 上下文加载完成 ==========\n");

        // 2. 构建最终Prompt
        StringBuilder promptBuilder = new StringBuilder();
        if (!memoryContext.isBlank()) {
            promptBuilder.append("用户长期记忆:\n").append(memoryContext).append("\n\n");
        }
        promptBuilder.append("用户问题:\n").append(userMessage);
        String finalPrompt = promptBuilder.toString();

        // 3. 打印 systemmessage
        String systemPrompt = chatPromptTemplates.getSystemPrompt();
        log.info("\n========== systemmessage ==========\n{}\n========== systemmessage end ==========\n", systemPrompt);

        // 4. 打印 humanmessage
        log.info("\n========== humanmessage ==========\n{}\n========== humanmessage end ==========\n", finalPrompt);

        // 5. 调用ReactAgent
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(String.valueOf(sessionId))
                .store(databaseStore)
                .build();

        try {
            AssistantMessage message = reactAgent.call(finalPrompt, runnableConfig);
            String response = message.getText();

            // 6. 打印 aimessage（完整的LLM返回内容）
            log.info("\n========== aimessage (LLM完整返回) ==========\n{}\n========== aimessage end ==========\n", response);

            // 7. 将完整响应拆分为字符流返回（模拟流式效果）
            return Flux.fromStream(response.chars().mapToObj(c -> String.valueOf((char) c)))
                    .delayElements(java.time.Duration.ofMillis(10));

        } catch (GraphRunnerException ex) {
            log.error("Agent执行异常: {}", ex.getMessage(), ex);
            return Flux.error(new BusinessException(ErrorCode.AI_GENERATE_ERROR, "健小助暂时无法完成本次查询: " + ex.getMessage()));
        }
    }
}
