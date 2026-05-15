package com.fitness.modules.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fitness.modules.chat.model.vo.DayPlanVO;
import com.fitness.modules.chat.model.vo.ExerciseVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FitnessPlanParser {

    private static final String[] DAY_NAMES = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final String TRAINING_TYPE_STRENGTH = "力量训练";
    private static final String TRAINING_TYPE_CARDIO = "有氧训练";
    private static final String TRAINING_TYPE_CORE = "核心训练";
    private static final String TRAINING_TYPE_REST = "休息恢复";
    private static final String TRAINING_TYPE_MIXED = "综合训练";
    private static final String DEFAULT_DIFFICULTY = "MEDIUM";
    private static final Pattern SETS_REPS_PATTERN =
            Pattern.compile("(\\d+)\\s*(?:组|sets?)\\s*[xX*×]\\s*(\\d+)\\s*(?:次|reps?)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIMPLE_SETS_REPS_PATTERN =
            Pattern.compile("(\\d+)\\s*[xX*×]\\s*(\\d+)");
    private static final Pattern DURATION_PATTERN =
            Pattern.compile("(\\d+)\\s*(?:分钟|min|mins|minutes?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern REST_PATTERN =
            Pattern.compile("(?:休息|rest)\\s*(\\d+)\\s*(?:秒|s|sec|seconds?)?", Pattern.CASE_INSENSITIVE);

    public List<DayPlanVO> parseWeeklyPlan(String aiResponse) {
        List<DayPlanVO> weeklyPlan = new ArrayList<>();
        DayPlanVO currentDay = null;

        for (String rawLine : aiResponse.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            int dayOfWeek = extractDayOfWeek(line);
            if (dayOfWeek > 0) {
                if (currentDay != null) {
                    finalizeDayPlan(currentDay);
                    weeklyPlan.add(currentDay);
                }
                currentDay = new DayPlanVO();
                currentDay.setDayOfWeek(dayOfWeek);
                currentDay.setDayName(DAY_NAMES[dayOfWeek]);
                currentDay.setTrainingType(extractTrainingType(line));
                currentDay.setFocus(extractFocus(line));
                currentDay.setExercises(new ArrayList<>());
                continue;
            }

            if (currentDay == null) {
                continue;
            }

            if (isTipsLine(line)) {
                currentDay.setDailyTips(stripLinePrefix(line));
                continue;
            }

            ExerciseVO exercise = parseExerciseLine(line);
            if (exercise != null && StrUtil.isNotBlank(exercise.getName())) {
                currentDay.getExercises().add(exercise);
            }
        }

        if (currentDay != null) {
            finalizeDayPlan(currentDay);
            weeklyPlan.add(currentDay);
        }

        return weeklyPlan.isEmpty() ? generateDefaultWeeklyPlan() : weeklyPlan;
    }

    public void finalizeDayPlan(DayPlanVO dayPlan) {
        List<ExerciseVO> exercises = dayPlan.getExercises();
        if (CollUtil.isEmpty(exercises)) {
            exercises = getDefaultExercisesForDay(dayPlan.getDayOfWeek());
            dayPlan.setExercises(exercises);
        }
        if (StrUtil.isBlank(dayPlan.getTrainingType())) {
            dayPlan.setTrainingType(inferTrainingType(dayPlan.getDayOfWeek()));
        }
        if (StrUtil.isBlank(dayPlan.getFocus())) {
            dayPlan.setFocus(defaultFocus(dayPlan.getDayOfWeek()));
        }
        if (dayPlan.getTotalDuration() == null) {
            int duration = exercises.stream()
                    .map(ExerciseVO::getDuration)
                    .filter(value -> value != null && value > 0)
                    .mapToInt(Integer::intValue)
                    .sum();
            dayPlan.setTotalDuration(duration > 0 ? duration : 60);
        }
        if (StrUtil.isBlank(dayPlan.getDailyTips())) {
            dayPlan.setDailyTips("注意保持动作规范，循序渐进。");
        }
    }

    public int extractDayOfWeek(String content) {
        String normalized = content.toLowerCase(Locale.ROOT);
        if (containsAny(content, "周一", "星期一") || normalized.contains("monday") || normalized.contains("day 1")) return 1;
        if (containsAny(content, "周二", "星期二") || normalized.contains("tuesday") || normalized.contains("day 2")) return 2;
        if (containsAny(content, "周三", "星期三") || normalized.contains("wednesday") || normalized.contains("day 3")) return 3;
        if (containsAny(content, "周四", "星期四") || normalized.contains("thursday") || normalized.contains("day 4")) return 4;
        if (containsAny(content, "周五", "星期五") || normalized.contains("friday") || normalized.contains("day 5")) return 5;
        if (containsAny(content, "周六", "星期六") || normalized.contains("saturday") || normalized.contains("day 6")) return 6;
        if (containsAny(content, "周日", "星期日", "星期天") || normalized.contains("sunday") || normalized.contains("day 7")) return 7;
        return 0;
    }

    public String extractTrainingType(String content) {
        String normalized = content.toLowerCase(Locale.ROOT);
        if (containsAny(content, "休息", "恢复") || normalized.contains("rest") || normalized.contains("recovery")) return TRAINING_TYPE_REST;
        if (containsAny(content, "有氧", "跑步", "骑行") || normalized.contains("cardio")) return TRAINING_TYPE_CARDIO;
        if (containsAny(content, "核心") || normalized.contains("core")) return TRAINING_TYPE_CORE;
        if (containsAny(content, "力量", "器械", "胸", "背", "肩", "腿") || normalized.contains("strength")) return TRAINING_TYPE_STRENGTH;
        return TRAINING_TYPE_MIXED;
    }

    public String extractFocus(String content) {
        if (containsAny(content, "胸")) return "胸部";
        if (containsAny(content, "背")) return "背部";
        if (containsAny(content, "肩")) return "肩部";
        if (containsAny(content, "腿")) return "腿部";
        if (containsAny(content, "核心", "腹")) return "核心";
        if (containsAny(content, "休息", "恢复")) return "恢复";
        return null;
    }

    public ExerciseVO parseExerciseLine(String line) {
        String normalizedLine = stripLinePrefix(line);
        if (normalizedLine.isEmpty()) return null;

        ExerciseVO exercise = new ExerciseVO();
        exercise.setName(extractExerciseName(normalizedLine));
        exercise.setRestTime(60);
        exercise.setDifficulty(DEFAULT_DIFFICULTY);

        Matcher setsRepsMatcher = SETS_REPS_PATTERN.matcher(normalizedLine);
        if (setsRepsMatcher.find()) {
            exercise.setSets(Integer.parseInt(setsRepsMatcher.group(1)));
            exercise.setReps(Integer.parseInt(setsRepsMatcher.group(2)));
        } else {
            Matcher simpleMatcher = SIMPLE_SETS_REPS_PATTERN.matcher(normalizedLine);
            if (simpleMatcher.find()) {
                exercise.setSets(Integer.parseInt(simpleMatcher.group(1)));
                exercise.setReps(Integer.parseInt(simpleMatcher.group(2)));
            }
        }

        Matcher durationMatcher = DURATION_PATTERN.matcher(normalizedLine);
        if (durationMatcher.find()) exercise.setDuration(Integer.parseInt(durationMatcher.group(1)));

        Matcher restMatcher = REST_PATTERN.matcher(normalizedLine);
        if (restMatcher.find()) exercise.setRestTime(Integer.parseInt(restMatcher.group(1)));

        int leftBracket = normalizedLine.indexOf('(');
        int rightBracket = normalizedLine.indexOf(')');
        if (leftBracket >= 0 && rightBracket > leftBracket) {
            exercise.setTips(normalizedLine.substring(leftBracket + 1, rightBracket).trim());
        }

        return exercise;
    }

    public List<DayPlanVO> generateDefaultWeeklyPlan() {
        List<DayPlanVO> weeklyPlan = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            DayPlanVO dayPlan = new DayPlanVO();
            dayPlan.setDayOfWeek(i);
            dayPlan.setDayName(DAY_NAMES[i]);
            dayPlan.setTrainingType(inferTrainingType(i));
            dayPlan.setFocus(defaultFocus(i));
            dayPlan.setExercises(getDefaultExercisesForDay(i));
            dayPlan.setTotalDuration(60);
            dayPlan.setDailyTips("注意保持动作规范，循序渐进。");
            weeklyPlan.add(dayPlan);
        }
        return weeklyPlan;
    }

    public List<ExerciseVO> getDefaultExercisesForDay(int dayOfWeek) {
        List<ExerciseVO> exercises = new ArrayList<>();
        switch (dayOfWeek) {
            case 1:
                exercises.add(createExercise("俯卧撑", 3, 15, null, "胸部"));
                exercises.add(createExercise("哑铃卧推", 4, 12, null, "胸部"));
                exercises.add(createExercise("飞鸟", 3, 15, null, "胸部"));
                break;
            case 2:
                exercises.add(createExercise("引体向上", 3, 8, null, "背部"));
                exercises.add(createExercise("哑铃划船", 4, 12, null, "背部"));
                exercises.add(createExercise("硬拉", 3, 10, null, "背部"));
                break;
            case 3:
                exercises.add(createExercise("跑步", null, null, 30, "全身"));
                exercises.add(createExercise("跳绳", null, null, 15, "全身"));
                break;
            case 4:
                exercises.add(createExercise("哑铃推举", 4, 12, null, "肩部"));
                exercises.add(createExercise("侧平举", 3, 15, null, "肩部"));
                exercises.add(createExercise("前平举", 3, 12, null, "肩部"));
                break;
            case 5:
                exercises.add(createExercise("深蹲", 4, 15, null, "腿部"));
                exercises.add(createExercise("弓步蹲", 3, 12, null, "腿部"));
                exercises.add(createExercise("提踵", 4, 20, null, "腿部"));
                break;
            case 6:
                exercises.add(createExercise("平板支撑", 3, null, 1, "核心"));
                exercises.add(createExercise("卷腹", 4, 20, null, "核心"));
                exercises.add(createExercise("俄罗斯转体", 3, 30, null, "核心"));
                break;
            case 7:
                exercises.add(createExercise("瑜伽拉伸", null, null, 30, "全身"));
                exercises.add(createExercise("散步", null, null, 20, "全身"));
                break;
            default:
                break;
        }
        return exercises;
    }

    public ExerciseVO createExercise(String name, Integer sets, Integer reps, Integer duration, String targetMuscle) {
        ExerciseVO exercise = new ExerciseVO();
        exercise.setName(name);
        exercise.setSets(sets);
        exercise.setReps(reps);
        exercise.setDuration(duration);
        exercise.setTargetMuscle(targetMuscle);
        exercise.setRestTime(60);
        exercise.setDifficulty(DEFAULT_DIFFICULTY);
        return exercise;
    }

    public String inferTrainingType(int dayOfWeek) {
        switch (dayOfWeek) {
            case 3: return TRAINING_TYPE_CARDIO;
            case 6: return TRAINING_TYPE_CORE;
            case 7: return TRAINING_TYPE_REST;
            default: return TRAINING_TYPE_STRENGTH;
        }
    }

    public String defaultFocus(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "胸部";
            case 2: return "背部";
            case 3: return "全身";
            case 4: return "肩部";
            case 5: return "腿部";
            case 6: return "核心";
            case 7: return "恢复";
            default: return "综合";
        }
    }

    public boolean isTipsLine(String line) {
        return containsAny(line, "注意", "建议", "提示", "tips", "note");
    }

    public String stripLinePrefix(String line) {
        return line.replaceFirst("^[\\-•*\\d.、\\s]+", "").trim();
    }

    public String extractExerciseName(String line) {
        String candidate = line.split("[（(:：-]")[0].trim();
        return StrUtil.blankToDefault(candidate, line);
    }

    public String getDayName(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) return "";
        return DAY_NAMES[dayOfWeek];
    }

    private boolean containsAny(String source, String... values) {
        for (String value : values) {
            if (source.contains(value)) return true;
        }
        return false;
    }
}