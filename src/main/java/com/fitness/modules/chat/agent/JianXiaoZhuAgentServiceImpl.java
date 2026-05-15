package com.fitness.modules.chat.agent;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.chat.memory.service.LongTermMemoryService;
import com.fitness.modules.chat.model.vo.ChatStreamEventVO;
import com.fitness.modules.chat.prompt.ChatPromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class JianXiaoZhuAgentServiceImpl implements JianXiaoZhuAgentService {

    private final ReactAgent reactAgent;
    private final DatabaseStore databaseStore;
    private final ChatPromptTemplates chatPromptTemplates;
    private final LongTermMemoryService longTermMemoryService;

    @Override
    public String chat(Long userId, Long sessionId, String userMessage) {
        String finalPrompt = buildPrompt(userId, userMessage);
        RunnableConfig runnableConfig = buildRunnableConfig(sessionId);
        try {
            Object result = reactAgent.call(finalPrompt, runnableConfig);
            return result != null ? result.toString() : "抱歉，健小助暂时无法完成本次回答，请稍后重试。";
        } catch (GraphRunnerException ex) {
            log.error("Agent call failed: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.AI_GENERATE_ERROR,
                    "健小助暂时无法完成本次查询: " + ex.getMessage());
        }
    }

    @Override
    public Flux<ChatStreamEventVO> streamChat(Long userId, Long sessionId, String userMessage) {
        String finalPrompt = buildPrompt(userId, userMessage);
        RunnableConfig runnableConfig = buildRunnableConfig(sessionId);
        Set<String> startedTools = ConcurrentHashMap.newKeySet();

        try {
            return reactAgent.stream(finalPrompt, runnableConfig)
                    .flatMap(output -> {
                        if (!(output instanceof StreamingOutput<?> streamingOutput)) {
                            return Flux.empty();
                        }

                        OutputType outputType = streamingOutput.getOutputType();
                        if (outputType == null) {
                            return Flux.empty();
                        }

                        if (outputType == OutputType.AGENT_MODEL_STREAMING) {
                            String chunk = streamingOutput.chunk();
                            if (chunk == null || chunk.isEmpty()) {
                                return Flux.empty();
                            }
                            return Flux.just(ChatStreamEventVO.delta(chunk));
                        }

                        if (outputType == OutputType.AGENT_TOOL_STREAMING) {
                            if (startedTools.add(output.node())) {
                                return Flux.just(ChatStreamEventVO.status("正在调用业务工具查询数据..."));
                            }
                            return Flux.empty();
                        }

                        if (outputType == OutputType.AGENT_TOOL_FINISHED) {
                            return Flux.just(ChatStreamEventVO.status("已获取查询结果，正在整理答案..."));
                        }

                        if (outputType == OutputType.AGENT_MODEL_FINISHED) {
                            return Flux.just(ChatStreamEventVO.status("正在输出最终回答..."));
                        }

                        return Flux.empty();
                    });
        } catch (GraphRunnerException ex) {
            log.error("Agent streaming failed: {}", ex.getMessage(), ex);
            return Flux.error(new BusinessException(ErrorCode.AI_GENERATE_ERROR,
                    "健小助暂时无法完成本次查询: " + ex.getMessage()));
        }
    }

    private String buildPrompt(Long userId, String userMessage) {
        String memoryContext = longTermMemoryService.buildMemoryContext(userId);
        return chatPromptTemplates.buildAgentUserPrompt(userMessage, memoryContext);
    }

    private RunnableConfig buildRunnableConfig(Long sessionId) {
        return RunnableConfig.builder()
                .threadId(String.valueOf(sessionId))
                .store(databaseStore)
                .build();
    }
}