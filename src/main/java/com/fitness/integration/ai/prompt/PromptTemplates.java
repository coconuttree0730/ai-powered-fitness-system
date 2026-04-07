package com.fitness.integration.ai.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI提示词模板管理类
 */
@Component
public class PromptTemplates {

    /**
     * 健身计划生成 Prompt 模板（返回结构化JSON）
     * 注意：使用字符串拼接避免 ST4 模板语法冲突
     * 格式约束由 BeanOutputConverter 自动生成
     */
    public String generateFitnessPlanJson(Map<String, Object> variables) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的健身教练AI助手。请根据学员的个人信息，制定一份科学的7天健身训练计划。\n\n");

        prompt.append("【核心要求 - 必须严格遵守】\n");
        prompt.append("1. 你只能从下方提供的【系统课程列表】和【系统器械列表】中选择数据\n");
        prompt.append("2. 绝对禁止编造或修改课程名称、器械名称、图片URL\n");
        prompt.append("3. 必须完整复制列表中的name、coverImage、image字段，不能有任何改动\n");
        prompt.append("4. 如果找不到完全匹配的课程或器械，请选择最接近的\n\n");

        prompt.append("学员档案信息：\n");
        prompt.append("- 身高：").append(variables.get("height")).append("cm\n");
        prompt.append("- 体重：").append(variables.get("weight")).append("kg\n");
        prompt.append("- 年龄：").append(variables.get("age")).append("岁\n");
        prompt.append("- 健身目标：").append(variables.get("goal")).append("\n");
        prompt.append("- 健身经验水平：").append(variables.get("experience")).append("\n\n");

        prompt.append("【系统课程列表】以下是系统中真实存在的课程，你必须从中选择：\n");
        prompt.append(variables.get("availableCourses")).append("\n\n");

        prompt.append("【系统器械列表】以下是系统中真实存在的器械，你必须从中选择：\n");
        prompt.append(variables.get("availableEquipment")).append("\n\n");

        prompt.append("【严格约束】\n");
        prompt.append("1. weeklyPlan必须包含7天（周一到周日）\n");
        prompt.append("2. 周日可以安排休息，course设为null，equipment设为空数组[]\n");
        prompt.append("3. course.name必须完全匹配【系统课程列表】中的name字段（一字不差）\n");
        prompt.append("4. course.coverImage必须完全匹配【系统课程列表】中的coverImage字段（一字不差）\n");
        prompt.append("5. equipment中的name必须完全匹配【系统器械列表】中的name字段（一字不差）\n");
        prompt.append("6. equipment中的image必须完全匹配【系统器械列表】中的image字段（一字不差）\n");
        prompt.append("7. 根据每天的focus（训练重点）智能匹配最合适的课程和器械\n");

        return prompt.toString();
    }

    /**
     * 健身计划生成 Prompt 模板（旧版兼容）
     */
    public String generateFitnessPlan(Map<String, Object> variables) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的健身教练。请为以下学员制定一份周度健身计划（7天）：\n\n");
        prompt.append("学员信息：\n");
        prompt.append("- 健身目标：").append(variables.get("goal")).append("\n");
        prompt.append("- 训练部位偏好：").append(variables.get("bodyPart")).append("\n");
        prompt.append("- 健身经验：").append(variables.get("experience")).append("\n");
        prompt.append("- 身高：").append(variables.get("height")).append("cm\n");
        prompt.append("- 体重：").append(variables.get("weight")).append("kg\n");
        prompt.append("- 年龄：").append(variables.get("age")).append("岁\n\n");
        prompt.append("请按照以下格式返回：\n");
        prompt.append("周一：...\n周二：...\n周三：...\n周四：...\n周五：...\n周六：...\n周日：...\n\n");
        prompt.append("每个训练日请包含：\n");
        prompt.append("1. 训练项目\n");
        prompt.append("2. 组数和次数\n");
        prompt.append("3. 训练时长\n");
        prompt.append("4. 注意事项\n\n");
        prompt.append("请确保计划科学合理，符合学员的身体状况和健身目标。");
        return prompt.toString();
    }

    /**
     * 营养建议 Prompt 模板
     */
    public String generateNutritionAdvice(Map<String, Object> variables) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的营养师。请根据以下信息提供营养建议：\n\n");
        prompt.append("用户信息：\n");
        prompt.append("- 健身目标：").append(variables.get("goal")).append("\n");
        prompt.append("- 身高：").append(variables.get("height")).append("cm\n");
        prompt.append("- 体重：").append(variables.get("weight")).append("kg\n");
        prompt.append("- 年龄：").append(variables.get("age")).append("岁\n");
        prompt.append("- 活动水平：").append(variables.get("activityLevel")).append("\n\n");
        prompt.append("请提供：\n");
        prompt.append("1. 每日热量摄入建议\n");
        prompt.append("2. 蛋白质、碳水化合物、脂肪摄入比例\n");
        prompt.append("3. 推荐的食物列表\n");
        prompt.append("4. 饮食时间安排建议");
        return prompt.toString();
    }

    /**
     * 健身数据分析 Prompt 模板
     */
    public String generateFitnessAnalysis(Map<String, Object> variables) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的健身教练。请分析以下健身数据并提供建议：\n\n");
        prompt.append("健身数据：\n");
        prompt.append(variables.get("data")).append("\n\n");
        prompt.append("请提供：\n");
        prompt.append("1. 数据分析总结\n");
        prompt.append("2. 训练效果评估\n");
        prompt.append("3. 改进建议\n");
        prompt.append("4. 下阶段训练重点");
        return prompt.toString();
    }

    /**
     * 运动动作指导 Prompt 模板
     */
    public String generateExerciseGuide(Map<String, Object> variables) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的健身教练。请为以下动作提供详细指导：\n\n");
        prompt.append("动作名称：").append(variables.get("exerciseName")).append("\n");
        prompt.append("目标肌群：").append(variables.get("targetMuscle")).append("\n");
        prompt.append("难度等级：").append(variables.get("difficulty")).append("\n\n");
        prompt.append("请提供：\n");
        prompt.append("1. 动作要领\n");
        prompt.append("2. 常见错误\n");
        prompt.append("3. 注意事项\n");
        prompt.append("4. 替代动作");
        return prompt.toString();
    }

    /**
     * 文本润色 System Prompt
     * 设定角色行为规范
     */
    public String getTextPolishSystemPrompt() {
        return """
                你是一位专业的健身房内容编辑。

                【你的职责】
                精准描述健身房的介绍（如设备介绍、课程简介等）。

                【必须遵守的规则】
                1. 只润色，不添加任何解释、说明或多余文字！！！
                2. 只输出一个最终润色结果，不提供选项
                3. 保持原文核心意思，不添加无关信息
                4. 语言专业，符合健身房场景

                【严格禁止】
                - 不要说"根据指令"、"这里提供"、"如需"、"可选"等
                - 不要解释你的润色思路
                - 不要分段、不要列点、不要编号
                - 只输出纯文本内容

                【例子】
                原文：“龙门架”；
                润色：“这款双滑轮龙门架设计专业，支持多样化的拉力训练，全方位刺激目标肌群。配备80kg配重系统，阻力调节灵活精准，满足从基础塑形到力量进阶的不同需求。其运行平稳流畅，是打造家庭健身房或提升商业健身区专业度的理想选择。”
                """;
    }

    /**
     * 文本润色 User Prompt 模板
     * 适用于设备管理、课程管理等表单描述字段的简短文本润色
     */
    public String generateTextPolishUserPrompt(String text) {
        return "原文：" + text + "\n\n润色后的文本：";
    }
}
