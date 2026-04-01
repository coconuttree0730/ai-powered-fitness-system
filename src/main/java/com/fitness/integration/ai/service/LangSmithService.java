package com.fitness.integration.ai.service;

import com.fitness.integration.ai.config.LangSmithConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * LangSmith 追踪服务
 * 用于记录和追踪 AI 调用过程，通过 LangSmith API 发送追踪数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LangSmithService {

    private final LangSmithConfig langSmithConfig;
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
        if (langSmithConfig.isAvailable()) {
            log.info("LangSmith 追踪服务已启用，项目: {}", langSmithConfig.getProjectName());
        } else {
            log.info("LangSmith 追踪服务未启用（未配置 API Key）");
        }
    }

    /**
     * 开始一个新的追踪会话
     *
     * @param operation 操作名称
     * @param metadata  元数据
     * @return 追踪会话 ID
     */
    public String startTrace(String operation, Map<String, Object> metadata) {
        if (!langSmithConfig.isAvailable()) {
            log.debug("LangSmith 未启用或配置不完整，跳过追踪");
            return null;
        }

        String traceId = UUID.randomUUID().toString();
        log.info("[LangSmith] 开始追踪 - Operation: {}, TraceId: {}", operation, traceId);

        Map<String, Object> traceInfo = new HashMap<>();
        traceInfo.put("trace_id", traceId);
        traceInfo.put("name", operation);
        traceInfo.put("project_name", langSmithConfig.getProjectName());
        traceInfo.put("start_time", Instant.now().toString());
        traceInfo.put("metadata", metadata);

        sendTraceToLangSmith(traceInfo);

        return traceId;
    }

    /**
     * 记录 LLM 调用输入
     *
     * @param traceId 追踪会话 ID
     * @param prompt  输入提示词
     * @param model   模型名称
     */
    public void logInput(String traceId, String prompt, String model) {
        if (traceId == null || !langSmithConfig.isAvailable()) {
            return;
        }

        log.info("[LangSmith] TraceId: {}, Model: {}, Prompt长度: {}",
                traceId, model, prompt != null ? prompt.length() : 0);

        Map<String, Object> runData = new HashMap<>();
        runData.put("trace_id", traceId);
        runData.put("name", "llm_call");
        runData.put("run_type", "llm");
        runData.put("inputs", Map.of("prompt", prompt != null ? prompt.substring(0, Math.min(prompt.length(), 1000)) : ""));
        runData.put("extra", Map.of("model", model));
        runData.put("start_time", Instant.now().toString());

        sendRunToLangSmith(runData);
    }

    /**
     * 记录 LLM 调用输出
     *
     * @param traceId  追踪会话 ID
     * @param response 响应内容
     * @param duration 耗时(毫秒)
     */
    public void logOutput(String traceId, String response, long duration) {
        if (traceId == null || !langSmithConfig.isAvailable()) {
            return;
        }

        log.info("[LangSmith] TraceId: {}, 响应长度: {}, 耗时: {}ms",
                traceId, response != null ? response.length() : 0, duration);

        Map<String, Object> runData = new HashMap<>();
        runData.put("trace_id", traceId);
        runData.put("outputs", Map.of("response", response != null ? response.substring(0, Math.min(response.length(), 1000)) : ""));
        runData.put("end_time", Instant.now().toString());
        runData.put("extra", Map.of("duration_ms", duration));

        sendRunToLangSmith(runData);
    }

    /**
     * 记录错误信息
     *
     * @param traceId 追踪会话 ID
     * @param error   错误信息
     */
    public void logError(String traceId, String error) {
        if (traceId == null || !langSmithConfig.isAvailable()) {
            return;
        }

        log.error("[LangSmith] TraceId: {}, Error: {}", traceId, error);

        Map<String, Object> runData = new HashMap<>();
        runData.put("trace_id", traceId);
        runData.put("error", error);
        runData.put("end_time", Instant.now().toString());

        sendRunToLangSmith(runData);
    }

    /**
     * 结束追踪会话
     *
     * @param traceId 追踪会话 ID
     */
    public void endTrace(String traceId) {
        if (traceId == null || !langSmithConfig.isAvailable()) {
            return;
        }

        log.info("[LangSmith] 结束追踪 - TraceId: {}", traceId);

        Map<String, Object> traceData = new HashMap<>();
        traceData.put("trace_id", traceId);
        traceData.put("end_time", Instant.now().toString());

        sendTraceToLangSmith(traceData);
    }

    /**
     * 获取 LangSmith 项目链接
     *
     * @return 项目链接
     */
    public String getProjectUrl() {
        if (!langSmithConfig.isAvailable()) {
            return null;
        }
        return langSmithConfig.getEndpoint() + "/projects/" + langSmithConfig.getProjectName();
    }

    /**
     * 异步发送追踪数据到 LangSmith
     */
    @Async
    protected void sendTraceToLangSmith(Map<String, Object> traceData) {
        if (!langSmithConfig.isAvailable()) {
            return;
        }

        try {
            String url = langSmithConfig.getEndpoint() + "/api/v1/traces";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", langSmithConfig.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(traceData, headers);
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            log.debug("发送 LangSmith 追踪数据失败: {}", e.getMessage());
        }
    }

    /**
     * 异步发送运行数据到 LangSmith
     */
    @Async
    protected void sendRunToLangSmith(Map<String, Object> runData) {
        if (!langSmithConfig.isAvailable()) {
            return;
        }

        try {
            String url = langSmithConfig.getEndpoint() + "/api/v1/runs";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", langSmithConfig.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(runData, headers);
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            log.debug("发送 LangSmith 运行数据失败: {}", e.getMessage());
        }
    }
}
