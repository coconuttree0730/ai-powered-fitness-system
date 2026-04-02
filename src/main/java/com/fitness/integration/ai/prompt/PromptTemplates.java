package com.fitness.integration.ai.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI Prompt 模板管理类
 * 定义系统中使用的各种 Prompt 模板
 */
@Component
public class PromptTemplates {

    /**
     * 健身计划生成 Prompt 模板
     */
    private static final String FITNESS_PLAN_TEMPLATE = """
            你是一位专业的健身教练。请为以下学员制定一份周度健身计划（7天）：

            学员信息：
            - 健身目标：{goal}
            - 训练部位偏好：{bodyPart}
            - 健身经验：{experience}
            - 身高：{height}cm
            - 体重：{weight}kg
            - 年龄：{age}岁

            请按照以下格式返回：
            周一：...
            周二：...
            周三：...
            周四：...
            周五：...
            周六：...
            周日：...

            每个训练日请包含：
            1. 训练项目
            2. 组数和次数
            3. 训练时长
            4. 注意事项

            请确保计划科学合理，符合学员的身体状况和健身目标。

            输出返回格式，html + css + js 生成的动态交互性好的精美的html页面
            """;

    /**
     * 健身数据分析 Prompt 模板
     */
    private static final String FITNESS_ANALYSIS_TEMPLATE = """
            你是一位专业的健身数据分析师。请根据以下用户的健身数据进行分析并给出建议：

            用户基本信息：
            - 年龄：{age}岁
            - 性别：{gender}
            - 身高：{height}cm
            - 体重：{weight}kg
            - BMI：{bmi}

            近期运动数据：
            - 总运动次数：{totalWorkouts}次
            - 总运动时长：{totalDuration}分钟
            - 平均每次运动时长：{avgDuration}分钟
            - 最常进行的运动类型：{favoriteType}
            - 消耗总卡路里：{totalCalories}千卡

            请提供以下分析：
            1. 身体状况评估
            2. 运动习惯分析
            3. 改进建议
            4. 下阶段目标建议
            """;

    /**
     * 营养建议 Prompt 模板
     */
    private static final String NUTRITION_ADVICE_TEMPLATE = """
            你是一位专业的运动营养师。请根据以下信息提供营养建议：

            用户信息：
            - 健身目标：{goal}
            - 当前体重：{weight}kg
            - 目标体重：{targetWeight}kg
            - 每日活动量：{activityLevel}
            - 饮食偏好：{dietPreference}
            - 过敏/忌口：{allergies}

            请提供：
            1. 每日热量需求估算
            2. 三大营养素比例建议
            3. 一日三餐示例
            4. 运动前后饮食建议
            5. 补剂建议（如有需要）
            """;

    /**
     * 运动动作指导 Prompt 模板
     */
    private static final String EXERCISE_GUIDE_TEMPLATE = """
            你是一位专业的健身教练。请针对以下运动动作提供详细指导：

            动作名称：{exerciseName}
            用户经验水平：{experienceLevel}
            训练目标：{trainingGoal}

            请提供：
            1. 动作要领（详细步骤）
            2. 目标肌群
            3. 推荐组数和次数
            4. 常见错误及纠正方法
            5. 安全注意事项
            6. 进阶/退阶变式
            """;

    /**
     * 文本润色 Prompt 模板
     */
    private static final String TEXT_POLISH_TEMPLATE = """
            你是一位专业的文案编辑。请对以下文本进行润色：

            %s

            润色要求：
            1. 保持原有核心信息和事实不变
            2. 优化语言表述，使其更加流畅、专业、有吸引力
            3. 改善文本结构和逻辑，提升可读性
            4. 适当补充细节描述，使内容更丰富完整
            5. 使用专业、准确、易懂的语言风格
            6. 控制在50-150字之间

            请直接返回润色后的文本，不要添加任何解释或说明。
            """;

    /**
     * 生成健身计划
     *
     * @param variables 模板变量
     * @return 生成的 Prompt
     */
    public String generateFitnessPlan(Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(FITNESS_PLAN_TEMPLATE);
        return promptTemplate.render(variables);
    }

    /**
     * 生成健身数据分析
     *
     * @param variables 模板变量
     * @return 生成的 Prompt
     */
    public String generateFitnessAnalysis(Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(FITNESS_ANALYSIS_TEMPLATE);
        return promptTemplate.render(variables);
    }

    /**
     * 生成营养建议
     *
     * @param variables 模板变量
     * @return 生成的 Prompt
     */
    public String generateNutritionAdvice(Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(NUTRITION_ADVICE_TEMPLATE);
        return promptTemplate.render(variables);
    }

    /**
     * 生成运动动作指导
     *
     * @param variables 模板变量
     * @return 生成的 Prompt
     */
    public String generateExerciseGuide(Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(EXERCISE_GUIDE_TEMPLATE);
        return promptTemplate.render(variables);
    }

    /**
     * 生成文本润色Prompt
     *
     * @param text 原始文本
     * @return 生成的Prompt
     */
    public String generateTextPolish(String text) {
        return String.format(TEXT_POLISH_TEMPLATE, text);
    }

    /**
     * 使用自定义模板生成 Prompt
     *
     * @param template 模板字符串
     * @param variables 模板变量
     * @return 生成的 Prompt
     */
    public String generateWithCustomTemplate(String template, Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(template);
        return promptTemplate.render(variables);
    }
}
