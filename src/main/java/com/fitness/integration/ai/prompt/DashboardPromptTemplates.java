package com.fitness.integration.ai.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 仪表盘 AI 分析提示词模板。
 */
@Component
public class DashboardPromptTemplates {

    private static final String DASHBOARD_ANALYSIS_SYSTEM_PROMPT = """
            你是一名健身房经营分析师。

            要求：
            1. 基于提供的数据做分析，不得编造不存在的经营数据。
            2. 优先给出可执行建议，而不是泛泛而谈。
            3. 结论要有数据支撑，指出风险、机会和优先级。
            4. 输出使用中文，结构清晰，适合直接给管理者阅读。
            """;

    private static final String OVERALL_ANALYSIS_USER_TEMPLATE = """
            请基于以下经营数据生成综合运营分析报告。

            ## 会员数据
            - 总会员数：{totalMembers}
            - 活跃会员数：{activeMembers}
            - 会员活跃率：{activeRate}%

            ## 课程数据
            - 总课程数：{totalCourses}
            - 总预约数：{totalBookings}
            - 平均每课程预约数：{avgBookingPerCourse}

            ## 器械数据
            - 总器械数：{totalEquipment}
            - 正常器械：{normalEquipment}
            - 维护中器械：{maintenanceEquipment}
            - 待维修器械：{repairEquipment}
            - 停用器械：{offlineEquipment}
            - 器械完好率：{equipmentGoodRate}%

            ## 今日运营
            - 今日订单数：{todayOrders}
            - 今日营收：{todayRevenue}

            ## 到店高峰时段
            {peakHoursData}

            ## 课程分类统计
            {courseStatsData}

            ## 营收趋势
            {revenueTrendData}

            ## 用户增长趋势
            {userGrowthData}

            ## 报修统计
            {repairStatsData}

            ## 会员卡销售统计
            {memberCardStatsData}

            请按以下结构输出：
            1. 会员运营分析
            2. 课程运营分析
            3. 营收分析
            4. 器械管理分析
            5. 运营综合建议
            """;

    private static final String MEMBER_ANALYSIS_USER_TEMPLATE = """
            请基于以下会员经营数据生成会员专项分析。

            - 总会员数：{totalMembers}
            - 活跃会员数：{activeMembers}
            - 会员活跃率：{activeRate}%
            - 今日订单数：{todayOrders}
            - 今日营收：{todayRevenue}

            ## 到店高峰时段
            {peakHoursData}

            ## 用户增长趋势
            {userGrowthData}

            ## 会员卡销售统计
            {memberCardStatsData}

            请输出：
            1. 会员活跃度评估
            2. 会员增长分析
            3. 流失风险分析
            4. 消费行为分析
            5. 会员运营建议
            """;

    private static final String COURSE_ANALYSIS_USER_TEMPLATE = """
            请基于以下课程数据生成课程运营分析。

            - 总课程数：{totalCourses}
            - 总预约数：{totalBookings}
            - 平均每课程预约数：{avgBookingPerCourse}

            ## 课程分类统计
            {courseStatsData}

            ## 到店高峰时段
            {peakHoursData}

            请输出：
            1. 课程受欢迎程度分析
            2. 时段安排分析
            3. 课程结构优化建议
            4. 预约率提升建议
            5. 课程运营建议
            """;

    private static final String EQUIPMENT_ANALYSIS_USER_TEMPLATE = """
            请基于以下器械经营数据生成器械专项分析。

            - 总器械数：{totalEquipment}
            - 正常器械：{normalEquipment}
            - 维护中器械：{maintenanceEquipment}
            - 待维修器械：{repairEquipment}
            - 停用器械：{offlineEquipment}
            - 器械完好率：{equipmentGoodRate}%

            ## 报修统计
            {repairStatsData}

            请输出：
            1. 器械状态评估
            2. 维护与故障分析
            3. 风险点识别
            4. 采购与更新建议
            5. 器械管理优化策略
            """;

    public AiPromptSpec createOverallAnalysisPrompt(Map<String, Object> variables) {
        return new AiPromptSpec(
                DASHBOARD_ANALYSIS_SYSTEM_PROMPT,
                render(OVERALL_ANALYSIS_USER_TEMPLATE, variables)
        );
    }

    public AiPromptSpec createMemberAnalysisPrompt(Map<String, Object> variables) {
        return new AiPromptSpec(
                DASHBOARD_ANALYSIS_SYSTEM_PROMPT,
                render(MEMBER_ANALYSIS_USER_TEMPLATE, variables)
        );
    }

    public AiPromptSpec createCourseAnalysisPrompt(Map<String, Object> variables) {
        return new AiPromptSpec(
                DASHBOARD_ANALYSIS_SYSTEM_PROMPT,
                render(COURSE_ANALYSIS_USER_TEMPLATE, variables)
        );
    }

    public AiPromptSpec createEquipmentAnalysisPrompt(Map<String, Object> variables) {
        return new AiPromptSpec(
                DASHBOARD_ANALYSIS_SYSTEM_PROMPT,
                render(EQUIPMENT_ANALYSIS_USER_TEMPLATE, variables)
        );
    }

    public String generateOverallAnalysis(Map<String, Object> variables) {
        return createOverallAnalysisPrompt(variables).user();
    }

    public String generateMemberAnalysis(Map<String, Object> variables) {
        return createMemberAnalysisPrompt(variables).user();
    }

    public String generateCourseAnalysis(Map<String, Object> variables) {
        return createCourseAnalysisPrompt(variables).user();
    }

    public String generateEquipmentAnalysis(Map<String, Object> variables) {
        return createEquipmentAnalysisPrompt(variables).user();
    }

    private String render(String template, Map<String, Object> variables) {
        return new PromptTemplate(template).render(variables);
    }
}
