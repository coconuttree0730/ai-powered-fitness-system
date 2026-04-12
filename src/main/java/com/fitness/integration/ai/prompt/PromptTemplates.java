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
     * 使用 BeanOutputConverter 自动生成 JSON Schema 约束
     */
    public String generateFitnessPlanJson(Map<String, Object> variables) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一位专业的健身教练。请根据学员档案制定一份7天健身训练计划。\n\n");

        prompt.append("## 学员档案\n");
        prompt.append("- 身高：").append(variables.get("height")).append(" cm\n");
        prompt.append("- 体重：").append(variables.get("weight")).append(" kg\n");
        prompt.append("- 年龄：").append(variables.get("age")).append(" 岁\n");
        prompt.append("- 目标：").append(variables.get("goal")).append("\n");
        prompt.append("- 经验：").append(variables.get("experience")).append("\n\n");

        prompt.append("## 可用课程\n");
        prompt.append(variables.get("availableCourses")).append("\n\n");

        prompt.append("## 可用器械\n");
        prompt.append(variables.get("availableEquipment")).append("\n\n");

        prompt.append("## 计划要求\n");
        prompt.append("1. weeklyPlan 必须恰好包含7天（周一到周日）\n");
        prompt.append("2. 周日可以设置成休息日，当然，你可以分析安排在哪天，例如遵循3分化、4分化、5分化、...,如果是休息日，格式是：courses=null, equipment=[], exercises=[]\n");
        prompt.append("3. 训练日每天推荐2-4门课程，courses的category要与当天focus匹配\n");
        prompt.append("4. 课程和器械必须从提供的列表中选择，禁止编造，且必须和训练日的动作符合！且是随机的，不能每次都推荐那几门！\n");
        prompt.append("5. 器械选择要符合训练类型：力量日用力量器械，有氧日用有氧器械\n");
        prompt.append("6. 训练日每天必须包含6-8个具体训练动作(exercises)，每个动作包含：name(动作名称), sets(组数), reps(次数), restSeconds(休息秒数)\n");
        prompt.append("   - 力量训练日示例：杠铃深蹲(3组x8次,休息90秒)、硬拉(3组x5次,休息120秒)\n");
        prompt.append("   - 有氧训练日示例：跑步机慢跑(1组x30分钟,休息60秒)、动感单车(1组x20分钟,休息30秒)\n\n");

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
                原文："龙门架"；
                润色："这款双滑轮龙门架设计专业，支持多样化的拉力训练，全方位刺激目标肌群。配备80kg配重系统，阻力调节灵活精准，满足从基础塑形到力量进阶的不同需求。其运行平稳流畅，是打造家庭健身房或提升商业健身区专业度的理想选择。"
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
