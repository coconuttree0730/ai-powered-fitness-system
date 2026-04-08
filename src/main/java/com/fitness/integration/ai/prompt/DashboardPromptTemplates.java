package com.fitness.integration.ai.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 仪表盘AI分析 Prompt 模板管理类
 */
@Component
public class DashboardPromptTemplates {

    /**
     * 综合运营分析 Prompt 模板
     * 使用 ${variable} 格式，这是Spring AI PromptTemplate的默认语法
     */
    private static final String OVERALL_ANALYSIS_TEMPLATE = """
            你是一位专业的健身房运营分析师。请根据以下运营数据进行分析，并给出专业的建议：

            ## 会员数据
            - 总会员数：${totalMembers}
            - 活跃会员数：${activeMembers}
            - 会员活跃率：${activeRate}%

            ## 课程数据
            - 总课程数：${totalCourses}
            - 总预约数：${totalBookings}
            - 平均每课程预约数：${avgBookingPerCourse}

            ## 器材数据
            - 总器材数：${totalEquipment}
            - 正常器材数：${normalEquipment}
            - 维护中器材数：${maintenanceEquipment}
            - 待维修器材数：${repairEquipment}
            - 已停用器材数：${offlineEquipment}
            - 器材完好率：${equipmentGoodRate}%

            ## 今日运营数据
            - 今日订单数：${todayOrders}
            - 今日营收：¥${todayRevenue}

            ## 到店高峰时间
            ${peakHoursData}

            ## 课程分类统计
            ${courseStatsData}

            ## 营收趋势分析
            ${revenueTrendData}

            ## 用户增长趋势
            ${userGrowthData}

            ## 报修处理统计
            ${repairStatsData}

            ## 会员卡销售统计
            ${memberCardStatsData}

            请从以下几个方面进行深度分析：

            ### 1. 会员运营分析
            - 会员活跃度评估
            - 会员增长趋势分析
            - 会员留存风险预警
            - 提升会员活跃度的具体建议

            ### 2. 课程运营分析
            - 课程受欢迎程度排名
            - 课程时段安排合理性评估
            - 课程类型优化建议
            - 提升课程预约率的策略

            ### 3. 营收分析
            - 营收趋势分析
            - 营收增长点识别
            - 营收优化建议

            ### 4. 器材管理分析
            - 器材使用状况评估
            - 器材维护建议
            - 器材采购建议
            - 器材管理优化策略

            ### 5. 运营综合建议
            - 短期改进措施（1-2周内可实施）
            - 中期优化计划（1-3个月）
            - 长期发展战略（3个月以上）

            请用中文回答，格式清晰，建议具体可行，数据支撑充分。
            """;

    /**
     * 会员分析 Prompt 模板
     */
    private static final String MEMBER_ANALYSIS_TEMPLATE = """
            你是一位专业的健身房会员运营专家。请根据以下会员数据进行分析：

            ## 会员数据
            - 总会员数：${totalMembers}
            - 活跃会员数：${activeMembers}
            - 会员活跃率：${activeRate}%

            ## 今日运营数据
            - 今日订单数：${todayOrders}
            - 今日营收：¥${todayRevenue}

            ## 到店高峰时间分布
            ${peakHoursData}

            ## 用户增长趋势
            ${userGrowthData}

            ## 会员卡销售统计
            ${memberCardStatsData}

            请从以下几个方面进行深度分析：

            ### 1. 会员活跃度评估
            - 当前活跃度水平评估
            - 活跃度变化趋势分析
            - 影响活跃度的关键因素

            ### 2. 会员增长分析
            - 用户增长趋势分析
            - 增长来源分析
            - 增长潜力评估

            ### 3. 会员留存风险分析
            - 流失风险预警
            - 高风险会员特征识别
            - 留存率提升策略

            ### 4. 会员消费行为分析
            - 会员卡购买偏好
            - 消费时段分布
            - 消费金额分析

            ### 5. 会员运营建议
            - 提升会员活跃度的具体措施
            - 会员增长策略
            - 会员留存优化方案
            - 会员价值提升计划

            请用中文回答，格式清晰，建议具体可行，数据支撑充分。
            """;

    /**
     * 课程分析 Prompt 模板
     */
    private static final String COURSE_ANALYSIS_TEMPLATE = """
            你是一位专业的健身房课程运营专家。请根据以下课程数据进行分析：

            ## 课程数据
            - 总课程数：${totalCourses}
            - 总预约数：${totalBookings}
            - 平均每课程预约数：${avgBookingPerCourse}

            ## 课程分类统计
            ${courseStatsData}

            ## 到店高峰时间分布
            ${peakHoursData}

            请从以下几个方面进行深度分析：

            ### 1. 课程受欢迎程度分析
            - 热门课程TOP排名
            - 冷门课程识别
            - 课程受欢迎程度变化趋势

            ### 2. 课程时段安排分析
            - 高峰时段课程安排评估
            - 低峰时段优化建议
            - 课程时间分布合理性

            ### 3. 课程类型优化分析
            - 课程类型丰富度评估
            - 新课程开发建议
            - 课程结构调整方案

            ### 4. 课程预约率分析
            - 预约率影响因素
            - 提升预约率策略
            - 课程容量优化建议

            ### 5. 课程运营建议
            - 短期课程优化措施
            - 中期课程规划
            - 长期课程发展战略
            - 教练资源配置建议

            请用中文回答，格式清晰，建议具体可行，数据支撑充分。
            """;

    /**
     * 器材分析 Prompt 模板
     */
    private static final String EQUIPMENT_ANALYSIS_TEMPLATE = """
            你是一位专业的健身房器材管理专家。请根据以下器材数据进行分析：

            ## 器材数据
            - 总器材数：${totalEquipment}
            - 正常器材数：${normalEquipment}
            - 维护中器材数：${maintenanceEquipment}
            - 待维修器材数：${repairEquipment}
            - 已停用器材数：${offlineEquipment}
            - 器材完好率：${equipmentGoodRate}%

            ## 报修处理统计
            ${repairStatsData}

            请从以下几个方面进行深度分析：

            ### 1. 器材使用状况评估
            - 器材完好率评估
            - 器材使用频率分析
            - 器材老化程度评估

            ### 2. 器材维护分析
            - 维护频率评估
            - 维护效率分析
            - 维护成本评估
            - 维护流程优化建议

            ### 3. 器材故障分析
            - 故障类型统计
            - 故障原因分析
            - 故障预防措施

            ### 4. 器材采购建议
            - 急需采购器材清单
            - 采购优先级排序
            - 采购预算建议

            ### 5. 器材管理优化策略
            - 器材管理制度优化
            - 器材使用培训建议
            - 器材安全检查方案
            - 器材更新换代计划

            请用中文回答，格式清晰，建议具体可行，数据支撑充分。
            """;

    /**
     * 生成综合运营分析 Prompt
     *
     * @param variables 模板变量
     * @return 生成的 Prompt
     */
    public String generateOverallAnalysis(Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(OVERALL_ANALYSIS_TEMPLATE);
        return promptTemplate.render(variables);
    }

    /**
     * 生成会员分析 Prompt
     *
     * @param variables 模板变量
     * @return 生成的 Prompt
     */
    public String generateMemberAnalysis(Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(MEMBER_ANALYSIS_TEMPLATE);
        return promptTemplate.render(variables);
    }

    /**
     * 生成课程分析 Prompt
     *
     * @param variables 模板变量
     * @return 生成的 Prompt
     */
    public String generateCourseAnalysis(Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(COURSE_ANALYSIS_TEMPLATE);
        return promptTemplate.render(variables);
    }

    /**
     * 生成器材分析 Prompt
     *
     * @param variables 模板变量
     * @return 生成的 Prompt
     */
    public String generateEquipmentAnalysis(Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(EQUIPMENT_ANALYSIS_TEMPLATE);
        return promptTemplate.render(variables);
    }
}
