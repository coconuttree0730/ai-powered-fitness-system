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

        prompt.append("# Role\n");
        prompt.append("你是一位拥有15年经验的顶级健身教练AI，擅长根据学员身体数据制定科学、个性化的周期训练计划。\n\n");

        prompt.append("# Task\n");
        prompt.append("根据学员档案信息，制定一份**严格7天**的周度健身训练计划，返回结构化JSON。\n\n");

        prompt.append("# 学员档案\n");
        prompt.append("| 字段 | 值 |\n");
        prompt.append("|------|----|\n");
        prompt.append("| 身高 | ").append(variables.get("height")).append(" cm |\n");
        prompt.append("| 体重 | ").append(variables.get("weight")).append(" kg |\n");
        prompt.append("| 年龄 | ").append(variables.get("age")).append(" 岁 |\n");
        prompt.append("| 目标 | ").append(variables.get("goal")).append(" |\n");
        prompt.append("| 经验 | ").append(variables.get("experience")).append(" |\n\n");

        prompt.append("# 系统课程库（必须从中选择）\n");
        prompt.append(variables.get("availableCourses")).append("\n\n");

        prompt.append("# 系统器械库（必须从中选择）\n");
        prompt.append(variables.get("availableEquipment")).append("\n\n");

        prompt.append("# 输出格式约束 - JSON Schema\n");
        prompt.append("返回JSON必须严格符合以下结构：\n");
        prompt.append("- subtitle: 计划标题（简短有力，如\"7天增肌高级训练计划\"）\n");
        prompt.append("- userInfo: { height, weight, bmi(计算值), goal, intensity }\n");
        prompt.append("- weeklyPlan: **长度必须恰好为7的数组**，每个元素包含：\n");
        prompt.append("  * dayName: \"周一\"~\"周日\"，**仅此7个值，不可出现\"周一（次周）\"等额外天**\n");
        prompt.append("  * focus: 当天训练重点描述（如\"全身复合动作强化\"）\n");
        prompt.append("  * courses: 课程数组，**每天必须推荐2~3门课程**（训练日至少2门，最多3门不同课程组合，严禁只推荐1门）；休息日设为 null\n");
        prompt.append("    - 每门课程: { name, description, coverImage, duration, id, **category** }\n");
        prompt.append("    - name/coverImage/category 必须**一字不差**匹配课程库\n");
        prompt.append("    - **category必须与当天focus的训练目标一致**（如focus=胸部训练→category=力量训练）\n");
        prompt.append("  * equipment: 器械数组，从系统器械库中选择；休息日设为 []\n");
        prompt.append("    - name/image 必须**一字不差**匹配器械库\n");
        prompt.append("  * exercises: 动作数组 [{ name, sets, reps, restSeconds }]；休息日设为 []\n\n");

        prompt.append("# ⚠️ 关键规则（违反任一条将导致输出无效）\n");
        prompt.append("1. 【强制】weeklyPlan.length === 7，不多不少，必须是 周一~周日 各一个元素\n");
        prompt.append("2. 【强制】休息日的 courses=null, equipment=[], exercises=[] —— 不允许有课程、器械、动作、tips\n");
        prompt.append("3. 【强制】每天 courses 数量必须在2~3之间（训练日至少2门不同课程，最多3门，**绝对禁止只返回1门课程**）\n");
        prompt.append("4. 【强制】course.name / course.coverImage 必须与系统课程库完全一致，禁止编造\n");
        prompt.append("5. 【强制】equipment.name / equipment.image 必须与系统器械库完全一致，禁止编造\n");
        //prompt.append("6. 【强制】不要输出 tips 字段，不要输出任何提示建议类字段\n");
        prompt.append("7. 【强制】dayName 只能是：周一、周二、周三、周四、周五、周六、周日\n");
        prompt.append("8. 【强制】【课程匹配】每天推荐的courses的category必须与当天focus高度相关！例如：\n");
        prompt.append("   - focus含\"胸/背/肩/臂/腿/力量\" → 选择category=\"力量训练\"的课程\n");
        prompt.append("   - focus含\"有氧/燃脂/心肺\" → 选择category=\"有氧燃脂\"的课程\n");
        prompt.append("   - focus含\"核心/腹/瑜伽/柔韧\" → 选择category=\"瑜伽普拉提\"的课程\n");
        prompt.append("   - focus含\"拳击/格斗\" → 选择category=\"拳击格斗\"的课程\n");
        prompt.append("   - focus含\"舞蹈/操课\" → 选择category=\"舞蹈操课\"的课程\n");
        prompt.append("   - focus含\"康复/体态/矫正\" → 选择category=\"康复体态\"的课程\n");
        prompt.append("9. 【强制】【器械匹配】每天推荐的equipment必须与当天训练类型匹配！例如：\n");
        prompt.append("   - 力量训练日 → 选择STRENGTH/FREE_WEIGHT类型器械（史密斯机、深蹲架、哑铃套装、杠铃等）\n");
        prompt.append("   - 有氧训练日 → 选择CARDIO类型器械（跑步机、动感单车、划船机、椭圆机等）\n");
        prompt.append("   - 核心/瑜伽日 → 选择FUNCTIONAL类型器械（瑜伽垫、瑜伽球、弹力带、泡沫轴等）\n");
        prompt.append("10. 【强制】【器械多样性】不同训练日的equipment必须有所区别！严禁每天都推荐相同的器械组合！\n");
        prompt.append("    每天至少推荐2-3件不同的器械，且相邻两天尽量避免重复相同器械\n\n");

        prompt.append("# 设计原则\n");
        prompt.append("- 遵循渐进超负荷原则，强度合理递进\n");
        prompt.append("- 每周安排1天完全休息日（通常为周日），确保恢复\n");
        prompt.append("- 相邻训练日避免同一肌群连续高强度刺激\n");
        prompt.append("- 【重要】课程选择要与当天 focus 的训练目标高度匹配（category必须对应）\n");
        prompt.append("- 【重要】器械选择要符合当天训练类型（力量日用力量器械，有氧日用有氧器械）\n");
        prompt.append("- 【重要】每天使用2-3件不同器械，7天内的器械组合要有变化和丰富度\n");

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
