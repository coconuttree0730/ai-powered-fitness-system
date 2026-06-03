package com.fitness.modules.chat.agent;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.chat.memory.service.LongTermMemoryService;
import com.fitness.modules.chat.model.vo.ChatStreamEventVO;
import com.fitness.modules.chat.model.vo.ToolCallLog;
import com.fitness.modules.chat.prompt.ChatPromptTemplates;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ObjectMapper objectMapper;

    /** 暂存被中断的 InterruptionMetadata，key=sessionId */
    private final ConcurrentHashMap<Long, InterruptionMetadata> pendingInterruptions = new ConcurrentHashMap<>();

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
        Map<String, LocalDateTime> toolStartTimes = new ConcurrentHashMap<>();

        try {
            return reactAgent.stream(finalPrompt, runnableConfig)
                    .flatMap(output -> {
                        // HITL：检测到中断元数据，暂存并返回 approval_required 事件
                        if (output instanceof InterruptionMetadata interruption) {
                            log.info("[HITL] Agent interrupted for approval, sessionId={}, toolFeedbacks={}",
                                    sessionId, interruption.toolFeedbacks().size());
                            pendingInterruptions.put(sessionId, interruption);
                            return Flux.just(buildApprovalRequiredEvent(interruption));
                        }

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
                                toolStartTimes.put(output.node(), LocalDateTime.now());
                                log.info("[AgentTool] tool={}, action=start, sessionId={}", output.node(), sessionId);
                                return Flux.just(ChatStreamEventVO.status("正在调用业务工具查询数据..."));
                            }
                            return Flux.empty();
                        }

                        if (outputType == OutputType.AGENT_TOOL_FINISHED) {
                            LocalDateTime startTime = toolStartTimes.get(output.node());
                            long durationMs = startTime != null ?
                                    java.time.Duration.between(startTime, LocalDateTime.now()).toMillis() : -1;
                            log.info("[AgentTool] tool={}, action=finished, durationMs={}, sessionId={}",
                                    output.node(), durationMs, sessionId);
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

    /**
     * 将 InterruptionMetadata 转为 approval_required SSE 事件
     */
    private ChatStreamEventVO buildApprovalRequiredEvent(InterruptionMetadata interruption) {
        try {
            List<Map<String, Object>> pendingActions = new ArrayList<>();
            for (InterruptionMetadata.ToolFeedback feedback : interruption.toolFeedbacks()) {
                Map<String, Object> action = new HashMap<>();
                action.put("toolCallId", feedback.getId());
                action.put("toolName", feedback.getName());
                action.put("arguments", feedback.getArguments());
                action.put("description", feedback.getDescription());
                pendingActions.add(action);
            }
            String json = objectMapper.writeValueAsString(pendingActions);
            return ChatStreamEventVO.approvalRequired(json);
        } catch (Exception e) {
            log.error("Failed to serialize interruption metadata", e);
            return ChatStreamEventVO.error("操作需要确认，但序列化失败，请重试");
        }
    }

    @Override
    public Flux<ChatStreamEventVO> resumeWithApproval(Long sessionId, String threadId, boolean approved) {
        log.info("[HITL] resumeWithApproval: sessionId={}, threadId={}, approved={}", sessionId, threadId, approved);

        // 取出暂存的 InterruptionMetadata
        InterruptionMetadata pendingMetadata = pendingInterruptions.remove(sessionId);
        if (pendingMetadata == null) {
            log.warn("[HITL] No pending interruption found for sessionId={}", sessionId);
            return Flux.just(ChatStreamEventVO.error("未找到待确认的操作，可能已过期，请重新发起"));
        }

        // 构造带审批结果的 InterruptionMetadata
        InterruptionMetadata.ToolFeedback.FeedbackResult result =
                approved ? InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED
                         : InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED;

        InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder();
        for (InterruptionMetadata.ToolFeedback original : pendingMetadata.toolFeedbacks()) {
            feedbackBuilder.addToolFeedback(
                    InterruptionMetadata.ToolFeedback.builder()
                            .id(original.getId())
                            .name(original.getName())
                            .arguments(original.getArguments())
                            .description(original.getDescription())
                            .result(result)
                            .build()
            );
        }
        InterruptionMetadata humanFeedback = feedbackBuilder.build();

        // 构造 resumeConfig，通过 addHumanFeedback 传回审批结果
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .store(databaseStore)
                .addHumanFeedback(humanFeedback)
                .build();

        try {
            return reactAgent.stream("", resumeConfig)
                    .flatMap(output -> {
                        if (output instanceof InterruptionMetadata interruption) {
                            log.info("[HITL] Agent interrupted again after resume, sessionId={}", sessionId);
                            pendingInterruptions.put(sessionId, interruption);
                            return Flux.just(buildApprovalRequiredEvent(interruption));
                        }
                        if (!(output instanceof StreamingOutput<?> streamingOutput)) {
                            return Flux.empty();
                        }
                        OutputType outputType = streamingOutput.getOutputType();
                        if (outputType == null) return Flux.empty();

                        if (outputType == OutputType.AGENT_MODEL_STREAMING) {
                            String chunk = streamingOutput.chunk();
                            if (chunk == null || chunk.isEmpty()) return Flux.empty();
                            return Flux.just(ChatStreamEventVO.delta(chunk));
                        }
                        if (outputType == OutputType.AGENT_MODEL_FINISHED) {
                            return Flux.just(ChatStreamEventVO.status("正在输出最终回答..."));
                        }
                        return Flux.empty();
                    });
        } catch (GraphRunnerException ex) {
            log.error("HITL resume failed: {}", ex.getMessage(), ex);
            return Flux.error(new BusinessException(ErrorCode.AI_GENERATE_ERROR,
                    "恢复执行失败: " + ex.getMessage()));
        }
    }
}