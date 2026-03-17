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
     */
    private static final String OVERALL_ANALYSIS_TEMPLATE = """
            你是一位专业的健身房运营分析师。请根据以下运营数据进行分析，并给出专业的建议：

            ## 会员数据
            - 总会员数：{{totalMembers}}
            - 活跃会员数：{{activeMembers}}
            - 会员活跃率：{{activeRate}}%

            ## 课程数据
            - 总课程数：{{totalCourses}}
            - 总预约数：{{totalBookings}}
            - 平均每课程预约数：{{avgBookingPerCourse}}

            ## 器材数据
            - 总器材数：{{totalEquipment}}
            - 正常器材数：{{normalEquipment}}
            - 维修中器材数：{{maintenanceEquipment}}

            ## 到店高峰时间
            {{peakHoursData}}

            ## 课程分类统计
            {{courseStatsData}}

            请从以下几个方面进行分析：
            1. 会员运营分析
            2. 课程运营分析
            3. 器材管理分析
            4. 运营建议

            请用中文回答，格式清晰，建议具体可行。
            """;

    /**
     * 会员分析 Prompt 模板
     */
    private static final String MEMBER_ANALYSIS_TEMPLATE = """
            你是一位专业的健身房会员运营专家。请根据以下会员数据进行分析：

            ## 会员数据
            - 总会员数：{{totalMembers}}
            - 活跃会员数：{{activeMembers}}
            - 会员活跃率：{{activeRate}}%

            ## 到店高峰时间分布
            {{peakHoursData}}

            请从以下几个方面进行分析：
            1. 会员活跃度评估
            2. 会员流失风险分析
            3. 提升会员活跃度的建议
            4. 会员运营策略建议

            请用中文回答，格式清晰，建议具体可行。
            """;

    /**
     * 课程分析 Prompt 模板
     */
    private static final String COURSE_ANALYSIS_TEMPLATE = """
            你是一位专业的健身房课程运营专家。请根据以下课程数据进行分析：

            ## 课程数据
            - 总课程数：{{totalCourses}}
            - 总预约数：{{totalBookings}}
            - 平均每课程预约数：{{avgBookingPerCourse}}

            ## 课程分类统计
            {{courseStatsData}}

            请从以下几个方面进行分析：
            1. 课程受欢迎程度分析
            2. 课程时段安排建议
            3. 课程类型优化建议
            4. 提升课程预约率的策略

            请用中文回答，格式清晰，建议具体可行。
            """;

    /**
     * 器材分析 Prompt 模板
     */
    private static final String EQUIPMENT_ANALYSIS_TEMPLATE = """
            你是一位专业的健身房器材管理专家。请根据以下器材数据进行分析：

            ## 器材数据
            - 总器材数：{{totalEquipment}}
            - 正常器材数：{{normalEquipment}}
            - 维修中器材数：{{maintenanceEquipment}}
            - 器材完好率：{{equipmentGoodRate}}%

            请从以下几个方面进行分析：
            1. 器材使用状况评估
            2. 器材维护建议
            3. 器材采购建议
            4. 器材管理优化策略

            请用中文回答，格式清晰，建议具体可行。
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
