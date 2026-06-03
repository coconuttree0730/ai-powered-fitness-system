package com.fitness.modules.chat.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 工具调用日志
 */
@Data
public class ToolCallLog {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具参数摘要
     */
    private String argsSummary;

    /**
     * 调用开始时间
     */
    private LocalDateTime startTime;

    /**
     * 调用结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 调用结果摘要
     */
    private String resultSummary;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;
}
