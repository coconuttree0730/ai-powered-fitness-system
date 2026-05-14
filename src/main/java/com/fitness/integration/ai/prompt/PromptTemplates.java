package com.fitness.integration.ai.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PromptTemplates {

    private static final String FITNESS_PLAN_SYSTEM_PROMPT = """
            You are a professional fitness coach for a gym management platform.
            Generate safe, practical, and personalized guidance based only on the provided profile, course catalog, and equipment catalog.

            Rules:
            1. Do not invent courses, equipment, or venue facts.
            2. Keep the plan realistic, balanced, and aligned with common exercise science.
            3. When structured output is required, keep field names complete and semantically accurate.
            4. Do not include prompt explanations or meta commentary.
            5. Respond in Simplified Chinese.
            """;

    private static final String FITNESS_PLAN_USER_TEMPLATE = """
            Generate a 7-day training plan from the following member profile.

            Member profile:
            - Height: {height} cm
            - Weight: {weight} kg
            - Age: {age}
            - Goal: {goal}
            - Experience: {experience}

            Available courses:
            {availableCourses}

            Available equipment:
            {availableEquipment}

            Output requirements:
            1. weeklyPlan must contain exactly 7 days.
            2. Rest days are allowed. On a rest day use courses=null, equipment=[], exercises=[].
            3. Training days should prioritize 2 to 4 courses and match the daily focus.
            4. Courses and equipment must be selected only from the provided catalogs.
            5. Equipment must match the training type.
            6. Each training day must contain 6 to 8 exercises, and each exercise must include name, sets, reps, restSeconds.
            7. Keep the plan executable and avoid repeating the exact same arrangement every day.
            """;

    private static final String LEGACY_FITNESS_PLAN_USER_TEMPLATE = """
            Create a 7-day fitness plan for the following member.

            Member information:
            - Goal: {goal}
            - Preferred body part: {bodyPart}
            - Experience: {experience}
            - Height: {height} cm
            - Weight: {weight} kg
            - Age: {age}

            Write a natural-language answer that includes:
            1. Daily training theme
            2. Daily training items
            3. Sets, reps, or duration
            4. Notes and precautions

            Respond in Simplified Chinese.
            """;

    private static final String FITNESS_ANALYSIS_SYSTEM_PROMPT = """
            You are a professional fitness coach and data analyst.
            Provide an objective analysis, training evaluation, and next-step recommendations.
            Return the answer directly in Simplified Chinese.
            """;

    private static final String FITNESS_ANALYSIS_USER_TEMPLATE = """
            Analyze the following fitness data and provide:
            1. Summary
            2. Training effect evaluation
            3. Improvement suggestions
            4. Next-stage focus

            Data:
            {data}
            """;

    private static final String NUTRITION_SYSTEM_PROMPT = """
            You are a professional nutrition advisor.
            Provide balanced, practical dietary advice that fits the user's goal and physical condition.
            Avoid unsafe or exaggerated suggestions.
            Respond in Simplified Chinese.
            """;

    private static final String NUTRITION_USER_TEMPLATE = """
            Provide nutrition advice based on the following information:

            - Goal: {goal}
            - Height: {height} cm
            - Weight: {weight} kg
            - Age: {age}
            - Activity level: {activityLevel}

            Include:
            1. Daily calorie suggestion
            2. Protein, carbohydrate, and fat ratio suggestion
            3. Recommended foods
            4. Meal timing suggestions
            """;

    private static final String EXERCISE_GUIDE_SYSTEM_PROMPT = """
            You are a professional fitness coach.
            Explain movements clearly, accurately, and safely.
            Emphasize proper form and risk awareness.
            Respond in Simplified Chinese.
            """;

    private static final String EXERCISE_GUIDE_USER_TEMPLATE = """
            Provide guidance for the following exercise:
            - Exercise name: {exerciseName}
            - Target muscle: {targetMuscle}
            - Difficulty: {difficulty}

            Include:
            1. Key technique points
            2. Common mistakes
            3. Precautions
            4. Alternative exercises
            """;

    private static final String TEXT_POLISH_SYSTEM_PROMPT = """
            You are a professional fitness-industry content editor.

            Rules:
            1. Only polish the text. Do not add unrelated information.
            2. Output only the final polished result.
            3. Preserve the original meaning.
            4. Make the wording professional and natural for gym business scenarios.
            5. Respond in Simplified Chinese.
            """;

    private static final String TEXT_POLISH_USER_TEMPLATE = """
            Polish the following text and return only the polished result:

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
