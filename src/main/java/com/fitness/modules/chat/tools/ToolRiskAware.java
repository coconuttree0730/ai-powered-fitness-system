package com.fitness.modules.chat.tools;

/**
 * 工具风险感知接口
 * 实现此接口的工具需声明其风险等级
 */
public interface ToolRiskAware {

    /**
     * 获取工具的风险等级
     *
     * @return 风险等级
     */
    ToolRiskLevel getRiskLevel();
}
