package com.fitness.modules.chat.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.modules.chat.model.vo.ExerciseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ExerciseJsonSerializer {

    private static final String DEFAULT_DIFFICULTY = "MEDIUM";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ExerciseVO> parseExercises(String exercisesJson) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawExercises = objectMapper.readValue(exercisesJson, List.class);
            List<ExerciseVO> exercises = new ArrayList<>();
            for (Map<String, Object> raw : rawExercises) {
                ExerciseVO exercise = new ExerciseVO();
                exercise.setName((String) raw.get("name"));
                exercise.setSets(asInteger(raw.get("sets")));
                exercise.setReps(asInteger(raw.get("reps")));
                exercise.setDuration(asInteger(raw.get("duration")));
                exercise.setTips((String) raw.get("tips"));
                exercise.setRestTime(asInteger(raw.get("restTime")));
                exercise.setTargetMuscle((String) raw.get("targetMuscle"));
                exercise.setDifficulty((String) raw.getOrDefault("difficulty", DEFAULT_DIFFICULTY));
                exercises.add(exercise);
            }
            return exercises;
        } catch (Exception e) {
            log.warn("Failed to parse exercises JSON: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public String serializeExercises(List<ExerciseVO> exercises) {
        if (CollUtil.isEmpty(exercises)) return null;

        try {
            List<Map<String, Object>> rawExercises = new ArrayList<>();
            for (ExerciseVO exercise : exercises) {
                Map<String, Object> raw = new HashMap<>();
                raw.put("name", exercise.getName());
                raw.put("sets", exercise.getSets());
                raw.put("reps", exercise.getReps());
                raw.put("duration", exercise.getDuration());
                raw.put("restTime", exercise.getRestTime());
                raw.put("targetMuscle", exercise.getTargetMuscle());
                raw.put("tips", exercise.getTips());
                raw.put("difficulty", exercise.getDifficulty());
                rawExercises.add(raw);
            }
            return objectMapper.writeValueAsString(rawExercises);
        } catch (Exception e) {
            log.warn("Failed to serialize exercises JSON: {}", e.getMessage());
            return null;
        }
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String && StrUtil.isNotBlank((String) value)) return Integer.parseInt((String) value);
        return null;
    }
}