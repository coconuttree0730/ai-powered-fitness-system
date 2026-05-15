package com.fitness.integration.ai.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PromptTemplates {

    private static final String FITNESS_PLAN_SYSTEM_PROMPT = """
            你是健身房管理平台的专业健身教练。
            仅根据提供的会员资料、课程目录和器材目录，生成安全、实用且个性化的训练指导。

            规则：
            1. 不要编造课程、器材或场馆信息。
            2. 保持计划现实可行、均衡合理，符合运动科学常识。
            3. 结构化输出时，保持字段名称完整且语义准确。
            4. 不要包含提示词说明或元注释。
            5. 使用简体中文回复。
            """;

    private static final String FITNESS_PLAN_USER_TEMPLATE = """
            根据以下会员资料生成一份 7 天训练计划。

            会员资料：
            - 身高：{height} 厘米
            - 体重：{weight} 公斤
            - 年龄：{age}
            - 目标：{goal}
            - 训练经验：{experience}

            可用课程：
            {availableCourses}

            可用器材：
            {availableEquipment}

            输出要求：
            1. weeklyPlan 必须包含恰好 7 天。
            2. 允许休息日，休息日使用 courses=null, equipment=[], exercises=[]。
            3. 训练日应优先选择 2 到 4 门课程并匹配当日训练重点。
            4. 课程和器材必须从提供的目录中选择。
            5. 器材必须与训练类型匹配。
            6. 每个训练日必须包含 6 到 8 个动作，每个动作需包含 name、sets、reps、restSeconds。
            7. 计划应具有可执行性，避免每天重复相同的安排。
            """;

    private static final String LEGACY_FITNESS_PLAN_USER_TEMPLATE = """
            为以下会员创建一份 7 天健身计划。

            会员信息：
            - 目标：{goal}
            - 偏好部位：{bodyPart}
            - 训练经验：{experience}
            - 身高：{height} 厘米
            - 体重：{weight} 公斤
            - 年龄：{age}

            编写自然语言回答，包含：
            1. 每日训练主题
            2. 每日训练项目
            3. 组数、次数或时长
            4. 注意事项

            使用简体中文回复。
            """;

    private static final String FITNESS_ANALYSIS_SYSTEM_PROMPT = """
            你是专业的健身教练和数据分析师。
            提供客观的数据分析、训练评估和下一步建议。
            直接使用简体中文回复。
            """;

    private static final String FITNESS_ANALYSIS_USER_TEMPLATE = """
            分析以下健身数据并提供：
            1. 总结概览
            2. 训练效果评估
            3. 改进建议
            4. 下一阶段重点

            数据：
            {data}
            """;

    private static final String NUTRITION_SYSTEM_PROMPT = """
            你是专业的营养顾问。
            提供符合用户目标和身体状况的均衡、实用的饮食建议。
            避免不安全或夸大的建议。
            使用简体中文回复。
            """;

    private static final String NUTRITION_USER_TEMPLATE = """
            根据以下信息提供营养建议：

            - 目标：{goal}
            - 身高：{height} 厘米
            - 体重：{weight} 公斤
            - 年龄：{age}
            - 活动水平：{activityLevel}

            包含：
            1. 每日热量建议
            2. 蛋白质、碳水化合物和脂肪配比建议
            3. 推荐食物
            4. 进餐时间建议
            """;

    private static final String EXERCISE_GUIDE_SYSTEM_PROMPT = """
            你是专业的健身教练。
            清晰、准确、安全地讲解动作要领。
            强调正确的动作姿势和风险意识。
            使用简体中文回复。
            """;

    private static final String EXERCISE_GUIDE_USER_TEMPLATE = """
            为以下动作提供指导：
            - 动作名称：{exerciseName}
            - 目标肌肉：{targetMuscle}
            - 难度：{difficulty}

            包含：
            1. 关键技术要点
            2. 常见错误
            3. 注意事项
            4. 替代动作
            """;

    private static final String TEXT_POLISH_SYSTEM_PROMPT = """
            你是专业的健身行业内容编辑。

            规则：
            1. 仅润色文本，不要添加无关信息。
            2. 仅输出润色后的最终结果。
            3. 保留原意不变。
            4. 使措辞适合健身房业务场景，专业且自然。
            5. 使用简体中文回复。
            """;

    private static final String TEXT_POLISH_USER_TEMPLATE = """
            润色以下文本，仅返回润色后的结果：

            {text}
            """;

    public AiPromptSpec createFitnessPlanPrompt(Map<String, Object> variables) {
        return new AiPromptSpec(
                FITNESS_PLAN_SYSTEM_PROMPT,
                render(FITNESS_PLAN_USER_TEMPLATE, variables)
        );
    }

    public AiPromptSpec createLegacyFitnessPlanPrompt(Map<String, Object> variables) {
        return new AiPromptSpec(
                FITNESS_PLAN_SYSTEM_PROMPT,
                render(LEGACY_FITNESS_PLAN_USER_TEMPLATE, variables)
        );
    }

    public AiPromptSpec createFitnessAnalysisPrompt(Map<String, Object> variables) {
        return new AiPromptSpec(
                FITNESS_ANALYSIS_SYSTEM_PROMPT,
                render(FITNESS_ANALYSIS_USER_TEMPLATE, variables)
        );
    }

    public AiPromptSpec createNutritionAdvicePrompt(Map<String, Object> variables) {
        return new AiPromptSpec(
                NUTRITION_SYSTEM_PROMPT,
                render(NUTRITION_USER_TEMPLATE, variables)
        );
    }

    public AiPromptSpec createExerciseGuidePrompt(Map<String, Object> variables) {
        return new AiPromptSpec(
                EXERCISE_GUIDE_SYSTEM_PROMPT,
                render(EXERCISE_GUIDE_USER_TEMPLATE, variables)
        );
    }

    public AiPromptSpec createTextPolishPrompt(String text) {
        return new AiPromptSpec(
                TEXT_POLISH_SYSTEM_PROMPT,
                render(TEXT_POLISH_USER_TEMPLATE, Map.of("text", text))
        );
    }

    private String render(String template, Map<String, Object> variables) {
        return new PromptTemplate(template).render(variables);
    }
}
