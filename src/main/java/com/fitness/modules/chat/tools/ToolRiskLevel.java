package com.fitness.modules.chat.tools;

/**
 * 工具风险等级枚举
 * 用于 Agent 工具调用的安全管控
 */
public enum ToolRiskLevel {

    /**
     * 只读操作，无副作用
     */
    LOW,

    /**
     * 需要用户确认的操作（如预约、下单）
     */
    MEDIUM,

    /**
     * 需要审批的操作（如退款、删除）
     */
    HIGH
}
